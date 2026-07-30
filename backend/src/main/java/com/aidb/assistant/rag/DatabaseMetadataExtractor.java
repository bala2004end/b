package com.aidb.assistant.rag;

import com.aidb.assistant.dto.*;
import com.aidb.assistant.entity.ConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class DatabaseMetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMetadataExtractor.class);

    private final TargetDatabasePoolManager poolManager;

    public DatabaseMetadataExtractor(TargetDatabasePoolManager poolManager) {
        this.poolManager = poolManager;
    }

    public SchemaDTO extractMetadata(ConnectionConfig config) throws SQLException {

        log.info("Connecting to MySQL target database");
        try (Connection conn = poolManager.getDataSource(config).getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            SchemaDTO schema = new SchemaDTO();
            schema.setDatabaseName(config.getDatabaseName());
            
            List<TableDTO> tableList = new ArrayList<>();
            List<String> viewsList = new ArrayList<>();
            List<String> proceduresList = new ArrayList<>();

            // Extract Tables and Views
            try (ResultSet rsTables = metaData.getTables(config.getDatabaseName(), null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rsTables.next()) {
                    String tableName = rsTables.getString("TABLE_NAME");
                    String tableType = rsTables.getString("TABLE_TYPE");
                    String remarks = rsTables.getString("REMARKS");

                    if ("VIEW".equalsIgnoreCase(tableType)) {
                        viewsList.add(tableName);
                    }

                    TableDTO tableDTO = TableDTO.builder()
                            .tableName(tableName)
                            .tableType(tableType)
                            .remarks(remarks != null ? remarks : "")
                            .columns(extractColumns(metaData, config.getDatabaseName(), tableName))
                            .foreignKeys(extractForeignKeys(metaData, config.getDatabaseName(), tableName))
                            .indexes(extractIndexes(metaData, config.getDatabaseName(), tableName))
                            .build();

                    tableList.add(tableDTO);
                }
            }

            // Extract Procedures
            try (ResultSet rsProcs = metaData.getProcedures(config.getDatabaseName(), null, "%")) {
                while (rsProcs.next()) {
                    proceduresList.add(rsProcs.getString("PROCEDURE_NAME"));
                }
            } catch (Exception e) {
                log.warn("Could not extract procedures: {}", e.getMessage());
            }

            schema.setTables(tableList);
            schema.setTotalTables(tableList.size());
            schema.setViews(viewsList);
            schema.setTotalViews(viewsList.size());
            schema.setProcedures(proceduresList);
            schema.setTotalProcedures(proceduresList.size());

            return schema;
        }
    }

    private List<ColumnDTO> extractColumns(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        List<ColumnDTO> columns = new ArrayList<>();
        
        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet rsPk = metaData.getPrimaryKeys(catalog, null, tableName)) {
            while (rsPk.next()) {
                primaryKeys.add(rsPk.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            log.warn("Error fetching PK for {}: {}", tableName, e.getMessage());
        }

        Map<String, String[]> foreignKeys = new HashMap<>();
        try (ResultSet rsFk = metaData.getImportedKeys(catalog, null, tableName)) {
            while (rsFk.next()) {
                String fkCol = rsFk.getString("FKCOLUMN_NAME");
                String pkTab = rsFk.getString("PKTABLE_NAME");
                String pkCol = rsFk.getString("PKCOLUMN_NAME");
                foreignKeys.put(fkCol, new String[]{pkTab, pkCol});
            }
        } catch (Exception e) {
            log.warn("Error fetching FK for {}: {}", tableName, e.getMessage());
        }

        try (ResultSet rsCols = metaData.getColumns(catalog, null, tableName, "%")) {
            while (rsCols.next()) {
                String colName = rsCols.getString("COLUMN_NAME");
                String typeName = rsCols.getString("TYPE_NAME");
                int colSize = rsCols.getInt("COLUMN_SIZE");
                int nullable = rsCols.getInt("NULLABLE");
                String remarks = rsCols.getString("REMARKS");

                boolean isPk = primaryKeys.contains(colName);
                boolean isFk = foreignKeys.containsKey(colName);
                String fkRefTable = isFk ? foreignKeys.get(colName)[0] : null;
                String fkRefCol = isFk ? foreignKeys.get(colName)[1] : null;

                columns.add(ColumnDTO.builder()
                        .columnName(colName)
                        .dataType(typeName)
                        .columnSize(colSize)
                        .isNullable(nullable == DatabaseMetaData.columnNullable)
                        .isPrimaryKey(isPk)
                        .isForeignKey(isFk)
                        .fkReferencedTable(fkRefTable)
                        .fkReferencedColumn(fkRefCol)
                        .remarks(remarks != null ? remarks : "")
                        .build());
            }
        }

        return columns;
    }

    private List<ForeignKeyDTO> extractForeignKeys(DatabaseMetaData metaData, String catalog, String tableName) {
        List<ForeignKeyDTO> list = new ArrayList<>();
        try (ResultSet rs = metaData.getImportedKeys(catalog, null, tableName)) {
            while (rs.next()) {
                list.add(ForeignKeyDTO.builder()
                        .fkColumnName(rs.getString("FKCOLUMN_NAME"))
                        .pkTableName(rs.getString("PKTABLE_NAME"))
                        .pkColumnName(rs.getString("PKCOLUMN_NAME"))
                        .fkName(rs.getString("FK_NAME"))
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to get FK details for {}: {}", tableName, e.getMessage());
        }
        return list;
    }

    private List<IndexDTO> extractIndexes(DatabaseMetaData metaData, String catalog, String tableName) {
        List<IndexDTO> list = new ArrayList<>();
        try (ResultSet rs = metaData.getIndexInfo(catalog, null, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null) {
                    list.add(IndexDTO.builder()
                            .indexName(indexName)
                            .columnName(rs.getString("COLUMN_NAME"))
                            .isNonUnique(rs.getBoolean("NON_UNIQUE"))
                            .type("BTREE")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get Index details for {}: {}", tableName, e.getMessage());
        }
        return list;
    }
}

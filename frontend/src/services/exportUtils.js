export const exportToCSV = (filename, rows, columns) => {
  if (!rows || !rows.length) return;

  const cols = columns || Object.keys(rows[0]);
  let csvContent = 'data:text/csv;charset=utf-8,';
  
  // Header row
  csvContent += cols.map(c => `"${c}"`).join(',') + '\r\n';

  // Data rows
  rows.forEach(row => {
    const rowStr = cols.map(c => {
      const val = row[c] !== undefined && row[c] !== null ? row[c] : '';
      return `"${String(val).replace(/"/g, '""')}"`;
    }).join(',');
    csvContent += rowStr + '\r\n';
  });

  const encodedUri = encodeURI(csvContent);
  const link = document.createElement('a');
  link.setAttribute('href', encodedUri);
  link.setAttribute('download', `${filename || 'query_result'}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export const exportToJSON = (filename, data) => {
  if (!data) return;
  const jsonStr = JSON.stringify(data, null, 2);
  const blob = new Blob([jsonStr], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename || 'query_result'}.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

type CsvValue =
  string
  | number
  | null
  | undefined;

export function downloadCsv(
  fileName: string,
  rows: CsvValue[][]
): void {
  const csvContent = rows
    .map((row) =>
      row
        .map((value) => escapeCsvValue(value))
        .join(',')
    )
    .join('\r\n');

  const blob = new Blob(
    ['\uFEFF', csvContent],
    {
      type: 'text/csv;charset=utf-8'
    }
  );

  const downloadUrl =
    URL.createObjectURL(blob);

  const link = document.createElement('a');

  link.href = downloadUrl;
  link.download = fileName;
  link.click();

  URL.revokeObjectURL(downloadUrl);
}

function escapeCsvValue(value: CsvValue): string {
  const text = String(value ?? '')
    .replace(/"/g, '""');

  return `"${text}"`;
}
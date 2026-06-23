import { useState } from "react";
import { Button } from "./button";
import { Card } from "./card";
import { Upload, X, CheckCircle, AlertTriangle } from "lucide-react";

interface ImportWizardProps {
  type: string;
  expectedColumns: string[];
  onDownloadTemplate: () => void;
  onCommit: (rows: any[]) => Promise<void>;
  onCancel: () => void;
}

export function ImportWizard({ type, expectedColumns, onDownloadTemplate, onCommit, onCancel }: ImportWizardProps) {
  const [csvData, setCsvData] = useState("");
  const [parsedRows, setParsedRows] = useState<any[]>([]);
  const [step, setStep] = useState<"UPLOAD" | "PREVIEW" | "COMMITTING">("UPLOAD");
  const [error, setError] = useState<string | null>(null);

  function handleParse() {
    if (!csvData.trim()) {
      setError("Please paste some CSV data first.");
      return;
    }
    try {
      const lines = csvData.split("\n").filter(l => l.trim() !== "");
      if (lines.length < 2) {
        setError("Please include a header row and at least one data row.");
        return;
      }
      const headers = lines[0].split(",").map(h => h.trim());
      const missing = expectedColumns.filter(c => !headers.includes(c));
      if (missing.length > 0) {
        setError(`Missing expected columns: ${missing.join(", ")}`);
        return;
      }

      const rows = lines.slice(1).map(line => {
        const values = line.split(",");
        const obj: any = {};
        headers.forEach((h, i) => {
          obj[h] = values[i]?.trim() || "";
        });
        return obj;
      });

      setParsedRows(rows);
      setError(null);
      setStep("PREVIEW");
    } catch (e: any) {
      setError(e.message || "Failed to parse CSV.");
    }
  }

  async function handleCommit() {
    setStep("COMMITTING");
    try {
      await onCommit(parsedRows);
    } catch (e: any) {
      setError(e.message || "Failed to commit data.");
      setStep("PREVIEW");
    }
  }

  return (
    <div className="space-y-4">
      {step === "UPLOAD" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium">Paste CSV Data</h3>
            <Button variant="outline" size="sm" onClick={onDownloadTemplate}>
              Download Template
            </Button>
          </div>
          <p className="text-sm text-muted-foreground">
            Expected columns: {expectedColumns.join(", ")}
          </p>
          <textarea
            className="w-full h-48 p-3 text-sm font-mono border rounded-md bg-muted/50 focus:outline-none focus:ring-2 focus:ring-accent"
            placeholder="fullName,email,roles\nJohn Doe,john@example.com,FACULTY"
            value={csvData}
            onChange={(e) => setCsvData(e.target.value)}
          />
          {error && <p className="text-sm text-danger">{error}</p>}
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={onCancel}>Cancel</Button>
            <Button onClick={handleParse}>Preview Data</Button>
          </div>
        </div>
      )}

      {step === "PREVIEW" && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium">Preview {parsedRows.length} rows</h3>
          </div>
          {error && <p className="text-sm text-danger">{error}</p>}
          <div className="max-h-64 overflow-y-auto border rounded-md">
            <table className="w-full text-sm text-left">
              <thead className="bg-muted sticky top-0">
                <tr>
                  {expectedColumns.map(c => (
                    <th key={c} className="p-2 font-medium border-b">{c}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {parsedRows.map((row, i) => (
                  <tr key={i} className="border-b last:border-0">
                    {expectedColumns.map(c => (
                      <td key={c} className="p-2">{row[c]}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setStep("UPLOAD")}>Back</Button>
            <Button onClick={handleCommit}>Commit {parsedRows.length} Rows</Button>
          </div>
        </div>
      )}

      {step === "COMMITTING" && (
        <div className="py-12 text-center space-y-4">
          <div className="animate-spin size-8 border-4 border-accent border-t-transparent rounded-full mx-auto" />
          <p className="text-muted-foreground">Committing data to server...</p>
        </div>
      )}
    </div>
  );
}

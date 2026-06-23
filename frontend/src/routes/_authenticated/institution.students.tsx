import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, Loader2, Upload } from "lucide-react";
import { toast } from "sonner";
import { listStudents, createStudent, updateAcademics, bulkCreateStudents } from "@/lib/api/students";
import type { StudentProfile } from "@/lib/api/entities";
import { ApiError } from "@/lib/api/client";
import { PageHeader, DataState } from "@/components/common";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { ImportWizard } from "@/components/ui/import-wizard";
import { Badge } from "@/components/ui/badge";

export const Route = createFileRoute("/_authenticated/institution/students")({
  component: StudentsPage,
});

const errToast = (e: unknown) => toast.error(e instanceof ApiError ? e.detail : "Action failed");

function StudentsPage() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [edit, setEdit] = useState<StudentProfile | null>(null);
  const [isBulkOpen, setIsBulkOpen] = useState(false);

  const q = useQuery({ queryKey: ["students"], queryFn: listStudents });

  const create = useMutation({
    mutationFn: createStudent,
    onSuccess: () => { setCreateOpen(false); qc.invalidateQueries({ queryKey: ["students"] }); toast.success("Student onboarded — welcome email sent!"); },
    onError: errToast,
  });

  const academics = useMutation({
    mutationFn: ({ id, body }: { id: number; body: { cgpa?: number; activeBacklogs?: number; currentTerm?: number } }) =>
      updateAcademics(id, body),
    onSuccess: () => { setEdit(null); qc.invalidateQueries({ queryKey: ["students"] }); toast.success("Academics updated"); },
    onError: errToast,
  });

  const bulkMutation = useMutation({
    mutationFn: bulkCreateStudents,
    onSuccess: (results) => {
      qc.invalidateQueries({ queryKey: ["students"] });
      const successCount = results.filter((r) => r.success).length;
      const failCount = results.length - successCount;
      if (failCount === 0) {
        toast.success(`Successfully imported ${successCount} students. Welcome emails with login credentials have been sent!`);
      } else {
        toast.warning(`Imported ${successCount} students (${failCount} failed). Emails sent to successful imports.`);
      }
      setIsBulkOpen(false);
    },
    onError: errToast,
  });

  const rows = q.data ?? [];

  return (
    <div>
      <PageHeader
        title="Students"
        description="Onboard students and maintain academic standing. Each student receives a welcome email with portal URL and login credentials."
        actions={
          <div className="flex gap-3">
            <Button variant="outline" onClick={() => setIsBulkOpen(true)}>
              <Upload className="size-4 mr-2" /> Bulk Import
            </Button>
            <Button variant="accent" onClick={() => setCreateOpen(true)}>
              <Plus className="size-4" /> New student
            </Button>
          </div>
        }
      />

      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No students yet" emptyBody="Onboard your first student to get started.">
        <DataTable columns={["Roll no.", "Branch", "Batch", "CGPA", "Backlogs", "Term", ""]}>
          {rows.map((s, i) => (
            <Row key={s.id} index={i}>
              <Cell className="font-mono text-xs uppercase">{s.rollNumber}</Cell>
              <Cell>{s.branch ?? "—"}</Cell>
              <Cell className="font-mono">{s.batchYear ?? "—"}</Cell>
              <Cell className="font-mono font-medium">{Number(s.cgpa).toFixed(2)}</Cell>
              <Cell className="font-mono">{s.activeBacklogs}</Cell>
              <Cell className="font-mono">{s.currentTerm}</Cell>
              <Cell>
                <div className="flex justify-end">
                  <Button variant="outline" size="sm" onClick={() => setEdit(s)}>
                    <Pencil className="size-3.5" /> Academics
                  </Button>
                </div>
              </Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>

      {/* Create student */}
      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} title="Onboard student" description="Creates a student login and profile. A welcome email with login credentials will be sent automatically.">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const f = new FormData(e.currentTarget);
            create.mutate({
              fullName: f.get("fullName") as string,
              email: f.get("email") as string,
              password: f.get("password") as string,
              rollNumber: f.get("rollNumber") as string,
              branch: (f.get("branch") as string) || undefined,
              batchYear: f.get("batchYear") ? Number(f.get("batchYear")) : undefined,
            });
          }}
          className="space-y-4"
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Full name"><Input name="fullName" required /></Field>
            <Field label="Roll number"><Input name="rollNumber" required className="font-mono uppercase" /></Field>
          </div>
          <Field label="Email"><Input name="email" type="email" required /></Field>
          <Field label="Temp password" hint="Student will change this on first sign-in"><Input name="password" required /></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Branch" hint="e.g. CSE"><Input name="branch" /></Field>
            <Field label="Batch year"><Input name="batchYear" type="number" placeholder="2026" /></Field>
          </div>
          <div className="rounded-md bg-accent/5 border border-accent/20 p-3 text-sm text-muted-foreground">
            📧 A welcome email with portal URL and login credentials will be sent to the student automatically.
          </div>
          <SubmitRow pending={create.isPending} label="Onboard" onCancel={() => setCreateOpen(false)} />
        </form>
      </Dialog>

      {/* Edit academics */}
      <Dialog open={!!edit} onClose={() => setEdit(null)} title="Update academic standing" description={edit?.rollNumber}>
        {edit && (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              const f = new FormData(e.currentTarget);
              academics.mutate({
                id: edit.id,
                body: {
                  cgpa: Number(f.get("cgpa")),
                  activeBacklogs: Number(f.get("activeBacklogs")),
                  currentTerm: Number(f.get("currentTerm")),
                },
              });
            }}
            className="space-y-4"
          >
            <div className="grid gap-4 sm:grid-cols-3">
              <Field label="CGPA"><Input name="cgpa" type="number" step="0.01" defaultValue={Number(edit.cgpa)} /></Field>
              <Field label="Backlogs"><Input name="activeBacklogs" type="number" defaultValue={edit.activeBacklogs} /></Field>
              <Field label="Term"><Input name="currentTerm" type="number" defaultValue={edit.currentTerm} /></Field>
            </div>
            <SubmitRow pending={academics.isPending} label="Save" onCancel={() => setEdit(null)} />
          </form>
        )}
      </Dialog>

      {/* Bulk Import Wizard */}
      <Dialog open={isBulkOpen} onClose={() => setIsBulkOpen(false)} title="Bulk Import Students" maxWidth="3xl">
        <div className="mb-4 rounded-md bg-accent/5 border border-accent/20 p-3 text-sm text-muted-foreground">
          📧 Each successfully imported student will receive a welcome email with the portal URL and auto-generated login credentials.
        </div>
        <ImportWizard
          type="STUDENTS"
          expectedColumns={["fullName", "email", "rollNumber", "branch", "batchYear"]}
          onDownloadTemplate={() => {
            const csv = "fullName,email,rollNumber,branch,batchYear\nJohn Doe,john@example.com,CSE001,CSE,2026\nJane Smith,jane@example.com,ECE002,ECE,2026";
            const blob = new Blob([csv], { type: "text/csv" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "students_bulk_import_template.csv";
            a.click();
          }}
          onCommit={async (validRows) => {
            const students = validRows.map(row => ({
              fullName: row.fullName,
              email: row.email,
              rollNumber: row.rollNumber,
              branch: row.branch || undefined,
              batchYear: row.batchYear ? Number(row.batchYear) : undefined,
            }));
            await bulkMutation.mutateAsync(students);
          }}
          onCancel={() => setIsBulkOpen(false)}
        />
      </Dialog>
    </div>
  );
}

function SubmitRow({ pending, label, onCancel }: { pending: boolean; label: string; onCancel: () => void }) {
  return (
    <div className="flex justify-end gap-2 pt-1">
      <Button type="button" variant="ghost" onClick={onCancel}>Cancel</Button>
      <Button type="submit" variant="accent" disabled={pending}>
        {pending && <Loader2 className="size-4 animate-spin" />}
        {label}
      </Button>
    </div>
  );
}

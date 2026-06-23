import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  listDepartments, createDepartment,
  listPrograms, createProgram,
  listCourses, createCourse,
  listTerms, createTerm,
  listSections, createSection,
  getCurriculumItems, addCurriculumItem, deleteCurriculumItem
} from "@/lib/api/academics";
import { ApiError } from "@/lib/api/client";
import { PageHeader, TabBar, DataState } from "@/components/common";
import { StatusBadge } from "@/components/ui/badge";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input, Textarea } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Field } from "@/components/ui/field";

export const Route = createFileRoute("/_authenticated/institution/academics")({
  component: AcademicsPage,
});

type Tab = "departments" | "programs" | "courses" | "curriculum" | "terms" | "sections";

const errToast = (e: unknown) => toast.error(e instanceof ApiError ? e.detail : "Action failed");

function AcademicsPage() {
  const [tab, setTab] = useState<Tab>("departments");
  return (
    <div>
      <PageHeader
        title="Academic structure"
        description="Departments, programs, courses, curriculum, terms and sections."
        actions={
          <TabBar<Tab>
            value={tab}
            onChange={setTab}
            tabs={[
              { value: "departments", label: "Departments" },
              { value: "programs", label: "Programs" },
              { value: "courses", label: "Courses" },
              { value: "curriculum", label: "Curriculum" },
              { value: "terms", label: "Terms" },
              { value: "sections", label: "Sections" },
            ]}
          />
        }
      />
      {tab === "departments" && <DepartmentsTab />}
      {tab === "programs" && <ProgramsTab />}
      {tab === "courses" && <CoursesTab />}
      {tab === "curriculum" && <CurriculumTab />}
      {tab === "terms" && <TermsTab />}
      {tab === "sections" && <SectionsTab />}
    </div>
  );
}

function CreateButton({ onClick, label }: { onClick: () => void; label: string }) {
  return (
    <div className="mb-4 flex justify-end">
      <Button variant="accent" onClick={onClick}>
        <Plus className="size-4" /> {label}
      </Button>
    </div>
  );
}

function DepartmentsTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["departments"], queryFn: listDepartments });
  const m = useMutation({
    mutationFn: createDepartment,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["departments"] }); toast.success("Department created"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  return (
    <div>
      <CreateButton onClick={() => setOpen(true)} label="New department" />
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No departments">
        <DataTable columns={["Name", "Code"]}>
          {rows.map((d, i) => (
            <Row key={d.id} index={i}>
              <Cell className="font-medium">{d.name}</Cell>
              <Cell className="font-mono text-xs uppercase">{d.code}</Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>
      <Dialog open={open} onClose={() => setOpen(false)} title="New department">
        <form onSubmit={(e) => { e.preventDefault(); const f = new FormData(e.currentTarget); m.mutate({ name: f.get("name") as string, code: f.get("code") as string }); }} className="space-y-4">
          <Field label="Name"><Input name="name" required /></Field>
          <Field label="Code"><Input name="code" required className="font-mono uppercase" /></Field>
          <SubmitRow pending={m.isPending} onCancel={() => setOpen(false)} />
        </form>
      </Dialog>
    </div>
  );
}

function ProgramsTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["programs"], queryFn: listPrograms });
  const depts = useQuery({ queryKey: ["departments"], queryFn: listDepartments });
  const m = useMutation({
    mutationFn: createProgram,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["programs"] }); toast.success("Program created"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  return (
    <div>
      <CreateButton onClick={() => setOpen(true)} label="New program" />
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No programs">
        <DataTable columns={["Name", "Code", "Level", "Terms", "Credits"]}>
          {rows.map((p, i) => (
            <Row key={p.id} index={i}>
              <Cell className="font-medium">{p.name}</Cell>
              <Cell className="font-mono text-xs uppercase">{p.code}</Cell>
              <Cell className="text-muted-foreground">{p.level}</Cell>
              <Cell className="font-mono">{p.durationTerms}</Cell>
              <Cell className="font-mono">{p.totalCredits}</Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>
      <Dialog open={open} onClose={() => setOpen(false)} title="New program">
        <form onSubmit={(e) => { e.preventDefault(); const f = new FormData(e.currentTarget); m.mutate({ departmentId: Number(f.get("departmentId")), name: f.get("name") as string, code: f.get("code") as string, level: f.get("level") as string, durationTerms: Number(f.get("durationTerms")), totalCredits: Number(f.get("totalCredits")) }); }} className="space-y-4">
          <Field label="Department"><Select name="departmentId" required>{(depts.data ?? []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}</Select></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Name"><Input name="name" required /></Field>
            <Field label="Code"><Input name="code" required className="font-mono uppercase" /></Field>
          </div>
          <Field label="Level"><Select name="level" defaultValue="UNDERGRADUATE"><option>UNDERGRADUATE</option><option>POSTGRADUATE</option></Select></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Duration (terms)"><Input name="durationTerms" type="number" defaultValue={8} /></Field>
            <Field label="Total credits"><Input name="totalCredits" type="number" defaultValue={160} /></Field>
          </div>
          <SubmitRow pending={m.isPending} onCancel={() => setOpen(false)} />
        </form>
      </Dialog>
    </div>
  );
}

function CoursesTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["courses"], queryFn: listCourses });
  const depts = useQuery({ queryKey: ["departments"], queryFn: listDepartments });
  const m = useMutation({
    mutationFn: createCourse,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["courses"] }); toast.success("Course created"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  return (
    <div>
      <CreateButton onClick={() => setOpen(true)} label="New course" />
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No courses">
        <DataTable columns={["Title", "Code", "Credits", "Type"]}>
          {rows.map((c, i) => (
            <Row key={c.id} index={i}>
              <Cell className="font-medium">{c.title}</Cell>
              <Cell className="font-mono text-xs uppercase">{c.code}</Cell>
              <Cell className="font-mono">{c.creditHours}</Cell>
              <Cell><StatusBadge status={c.type} /></Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>
      <Dialog open={open} onClose={() => setOpen(false)} title="New course">
        <form onSubmit={(e) => { e.preventDefault(); const f = new FormData(e.currentTarget); m.mutate({ departmentId: Number(f.get("departmentId")), code: f.get("code") as string, title: f.get("title") as string, creditHours: Number(f.get("creditHours")), type: f.get("type") as string, description: (f.get("description") as string) || undefined }); }} className="space-y-4">
          <Field label="Department"><Select name="departmentId" required>{(depts.data ?? []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}</Select></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Title"><Input name="title" required /></Field>
            <Field label="Code"><Input name="code" required className="font-mono uppercase" /></Field>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Credit hours"><Input name="creditHours" type="number" defaultValue={3} /></Field>
            <Field label="Type"><Select name="type" defaultValue="CORE"><option>CORE</option><option>ELECTIVE</option><option>LAB</option></Select></Field>
          </div>
          <Field label="Description" hint="Optional"><Textarea name="description" /></Field>
          <SubmitRow pending={m.isPending} onCancel={() => setOpen(false)} />
        </form>
      </Dialog>
    </div>
  );
}

function TermsTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["terms"], queryFn: listTerms });
  const m = useMutation({
    mutationFn: createTerm,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["terms"] }); toast.success("Term created"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  return (
    <div>
      <CreateButton onClick={() => setOpen(true)} label="New term" />
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No terms">
        <DataTable columns={["Name", "Start", "End", "Status"]}>
          {rows.map((t, i) => (
            <Row key={t.id} index={i}>
              <Cell className="font-medium">{t.name}</Cell>
              <Cell className="font-mono text-xs">{t.startDate ?? "—"}</Cell>
              <Cell className="font-mono text-xs">{t.endDate ?? "—"}</Cell>
              <Cell><StatusBadge status={t.status} /></Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>
      <Dialog open={open} onClose={() => setOpen(false)} title="New term">
        <form onSubmit={(e) => { e.preventDefault(); const f = new FormData(e.currentTarget); m.mutate({ name: f.get("name") as string, startDate: (f.get("startDate") as string) || undefined, endDate: (f.get("endDate") as string) || undefined, status: f.get("status") as string }); }} className="space-y-4">
          <Field label="Name"><Input name="name" required placeholder="Autumn 2026" /></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Start date"><Input name="startDate" type="date" /></Field>
            <Field label="End date"><Input name="endDate" type="date" /></Field>
          </div>
          <Field label="Status"><Select name="status" defaultValue="PLANNED"><option>PLANNED</option><option>ACTIVE</option><option>CLOSED</option></Select></Field>
          <SubmitRow pending={m.isPending} onCancel={() => setOpen(false)} />
        </form>
      </Dialog>
    </div>
  );
}

function SectionsTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["sections"], queryFn: () => listSections() });
  const courses = useQuery({ queryKey: ["courses"], queryFn: listCourses });
  const terms = useQuery({ queryKey: ["terms"], queryFn: listTerms });
  const m = useMutation({
    mutationFn: createSection,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["sections"] }); toast.success("Section created"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  const courseName = (id: number) => courses.data?.find((c) => c.id === id)?.title ?? `#${id}`;
  const termName = (id: number) => terms.data?.find((t) => t.id === id)?.name ?? `#${id}`;
  return (
    <div>
      <CreateButton onClick={() => setOpen(true)} label="New section" />
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No sections">
        <DataTable columns={["Course", "Term", "Capacity", "Schedule"]}>
          {rows.map((s, i) => (
            <Row key={s.id} index={i}>
              <Cell className="font-medium">{courseName(s.courseId)}</Cell>
              <Cell>{termName(s.termId)}</Cell>
              <Cell className="font-mono">{s.capacity}</Cell>
              <Cell className="text-muted-foreground">{s.schedule ?? "—"}</Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>
      <Dialog open={open} onClose={() => setOpen(false)} title="New section">
        <form onSubmit={(e) => { e.preventDefault(); const f = new FormData(e.currentTarget); m.mutate({ courseId: Number(f.get("courseId")), termId: Number(f.get("termId")), capacity: Number(f.get("capacity")), schedule: (f.get("schedule") as string) || undefined }); }} className="space-y-4">
          <Field label="Course"><Select name="courseId" required>{(courses.data ?? []).map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}</Select></Field>
          <Field label="Term"><Select name="termId" required>{(terms.data ?? []).map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}</Select></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Capacity"><Input name="capacity" type="number" defaultValue={60} /></Field>
            <Field label="Schedule" hint="Optional"><Input name="schedule" placeholder="Mon/Wed 10:00" /></Field>
          </div>
          <SubmitRow pending={m.isPending} onCancel={() => setOpen(false)} />
        </form>
      </Dialog>
    </div>
  );
}

function SubmitRow({ pending, onCancel }: { pending: boolean; onCancel: () => void }) {
  return (
    <div className="flex justify-end gap-2 pt-1">
      <Button type="button" variant="ghost" onClick={onCancel}>Cancel</Button>
      <Button type="submit" variant="accent" disabled={pending}>
        {pending && <Loader2 className="size-4 animate-spin" />}
        Create
      </Button>
    </div>
  );
}

function CurriculumTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<number | null>(null);

  const programs = useQuery({ queryKey: ["programs"], queryFn: listPrograms });
  const courses = useQuery({ queryKey: ["courses"], queryFn: listCourses });
  const curriculum = useQuery({ 
    queryKey: ["curriculum", selectedProgram], 
    queryFn: () => getCurriculumItems(selectedProgram!), 
    enabled: !!selectedProgram 
  });

  const addMut = useMutation({
    mutationFn: addCurriculumItem,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["curriculum", selectedProgram] }); toast.success("Course added to curriculum"); },
    onError: errToast,
  });

  const delMut = useMutation({
    mutationFn: deleteCurriculumItem,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["curriculum", selectedProgram] }); toast.success("Removed from curriculum"); },
    onError: errToast,
  });

  const courseName = (id: number) => courses.data?.find((c) => c.id === id)?.title ?? `#${id}`;
  const courseCode = (id: number) => courses.data?.find((c) => c.id === id)?.code ?? "";

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4 bg-muted/30 p-4 rounded-md border border-border">
        <label className="text-sm font-medium">Select Program:</label>
        <Select 
          value={selectedProgram ?? ""} 
          onChange={(e) => setSelectedProgram(Number(e.target.value))}
          className="max-w-xs"
        >
          <option value="" disabled>Select a program...</option>
          {(programs.data ?? []).map((p) => (
            <option key={p.id} value={p.id}>{p.name} ({p.code})</option>
          ))}
        </Select>
      </div>

      {selectedProgram ? (
        <>
          <CreateButton onClick={() => setOpen(true)} label="Add Course to Curriculum" />
          <DataState isLoading={curriculum.isLoading} error={curriculum.error} isEmpty={(curriculum.data ?? []).length === 0} emptyTitle="No curriculum defined">
            <DataTable columns={["Term", "Course Code", "Course Title", "Mandatory", "Action"]}>
              {(curriculum.data ?? []).sort((a,b) => a.termNumber - b.termNumber).map((item, i) => (
                <Row key={item.id} index={i}>
                  <Cell className="font-mono">Term {item.termNumber}</Cell>
                  <Cell className="font-mono text-xs uppercase">{courseCode(item.courseId)}</Cell>
                  <Cell className="font-medium">{courseName(item.courseId)}</Cell>
                  <Cell>{item.mandatory ? "Yes" : "No"}</Cell>
                  <Cell>
                    <Button variant="ghost" size="sm" className="text-destructive hover:bg-destructive/10 hover:text-destructive" onClick={() => delMut.mutate(item.id)} disabled={delMut.isPending}>
                      Remove
                    </Button>
                  </Cell>
                </Row>
              ))}
            </DataTable>
          </DataState>

          <Dialog open={open} onClose={() => setOpen(false)} title="Add Course to Curriculum">
            <form onSubmit={(e) => { 
              e.preventDefault(); 
              const f = new FormData(e.currentTarget); 
              addMut.mutate({ 
                programId: selectedProgram, 
                courseId: Number(f.get("courseId")), 
                termNumber: Number(f.get("termNumber")), 
                mandatory: f.get("mandatory") === "on" 
              }); 
            }} className="space-y-4">
              <Field label="Course">
                <Select name="courseId" required>
                  {(courses.data ?? []).map((c) => <option key={c.id} value={c.id}>{c.code} - {c.title}</option>)}
                </Select>
              </Field>
              <Field label="Term Number">
                <Input name="termNumber" type="number" min={1} required defaultValue={1} />
              </Field>
              <div className="flex items-center gap-2">
                <input type="checkbox" name="mandatory" id="mandatory" defaultChecked className="size-4" />
                <label htmlFor="mandatory" className="text-sm">Mandatory Course</label>
              </div>
              <SubmitRow pending={addMut.isPending} onCancel={() => setOpen(false)} />
            </form>
          </Dialog>
        </>
      ) : (
        <div className="py-12 text-center text-muted-foreground border border-dashed border-border rounded-md">
          Please select a program to manage its curriculum.
        </div>
      )}
    </div>
  );
}


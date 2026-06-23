import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { listPrograms, listTerms } from "@/lib/api/academics";
import { getFeeStructures, createFeeStructure, getAllInvoices, generateBulkInvoices } from "@/lib/api/finance";
import { PageHeader, moneyINR, shortDate } from "@/components/common";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { Select } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { FadeUp } from "@/components/motion/FadeUp";

export const Route = createFileRoute("/_authenticated/institution/fees")({
  component: InstitutionFeesPage,
});

function InstitutionFeesPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"structures" | "invoices">("structures");
  const [isStructureOpen, setIsStructureOpen] = useState(false);
  const [isBulkInvoiceOpen, setIsBulkInvoiceOpen] = useState(false);

  const programsQuery = useQuery({ queryKey: ["programs"], queryFn: listPrograms });
  const termsQuery = useQuery({ queryKey: ["terms"], queryFn: listTerms });
  const structuresQuery = useQuery({ queryKey: ["feeStructures"], queryFn: () => getFeeStructures() });
  const invoicesQuery = useQuery({
    queryKey: ["invoices"],
    queryFn: () => getAllInvoices().then((res) => res.content),
    enabled: activeTab === "invoices",
  });

  const createStructMutation = useMutation({
    mutationFn: createFeeStructure,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["feeStructures"] });
      toast.success("Fee structure created");
      setIsStructureOpen(false);
    },
    onError: (err: any) => toast.error(err.detail || "Failed to create fee structure"),
  });

  const generateBulkMutation = useMutation({
    mutationFn: (feeStructureId: number) => generateBulkInvoices(feeStructureId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["invoices"] });
      toast.success(`Bulk invoice generation completed.`);
      setIsBulkInvoiceOpen(false);
    },
    onError: (err: any) => toast.error(err.detail || "Failed to generate bulk invoices"),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Fee Management"
        description="Configure fee structures and manage student invoices."
        actions={
          <div className="flex gap-2 bg-muted/50 p-1 rounded-lg">
            <button
              onClick={() => setActiveTab("structures")}
              className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${
                activeTab === "structures" ? "bg-background shadow text-foreground" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Structures
            </button>
            <button
              onClick={() => setActiveTab("invoices")}
              className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${
                activeTab === "invoices" ? "bg-background shadow text-foreground" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Invoices
            </button>
          </div>
        }
      />

      <FadeUp>
        {activeTab === "structures" && (
          <Card className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-semibold">Fee Structures</h2>
              <Button variant="accent" onClick={() => setIsStructureOpen(true)}>New Structure</Button>
              <Dialog open={isStructureOpen} onClose={() => setIsStructureOpen(false)} title="Create Fee Structure">
                  <form
                    className="grid gap-4 mt-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      const fd = new FormData(e.currentTarget);
                      createStructMutation.mutate({
                        programId: Number(fd.get("programId")),
                        termId: Number(fd.get("termId")),
                        name: fd.get("name") as string,
                        amount: Number(fd.get("amount")),
                        feeType: fd.get("feeType") as string,
                        dueDate: fd.get("dueDate") as string,
                      });
                    }}
                  >
                    <div className="grid grid-cols-2 gap-4">
                      <Field label="Program">
                        <Select name="programId" required>
                          <option value="">Select Program...</option>
                          {programsQuery.data?.map((p) => (
                            <option key={p.id} value={p.id}>{p.name} ({p.code})</option>
                          ))}
                        </Select>
                      </Field>
                      <Field label="Academic Term">
                        <Select name="termId" required>
                          <option value="">Select Term...</option>
                          {termsQuery.data?.map((t) => (
                            <option key={t.id} value={t.id}>{t.name}</option>
                          ))}
                        </Select>
                      </Field>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <Field label="Fee Name">
                        <Input name="name" required placeholder="e.g. Tuition Fee - Semester 1" />
                      </Field>
                      <Field label="Fee Type">
                        <Select name="feeType" required>
                          <option value="TUITION">Tuition</option>
                          <option value="HOSTEL">Hostel</option>
                          <option value="LAB">Lab</option>
                          <option value="EXAM">Exam</option>
                          <option value="MISC">Miscellaneous</option>
                        </Select>
                      </Field>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <Field label="Amount (₹)">
                        <Input name="amount" type="number" required placeholder="50000" />
                      </Field>
                      <Field label="Due Date">
                        <Input name="dueDate" type="date" required />
                      </Field>
                    </div>
                    <div className="flex justify-end gap-2 mt-4">
                      <Button variant="outline" type="button" onClick={() => setIsStructureOpen(false)}>Cancel</Button>
                      <Button variant="accent" type="submit" disabled={createStructMutation.isPending}>
                        {createStructMutation.isPending ? "Creating..." : "Create"}
                      </Button>
                    </div>
                  </form>
              </Dialog>
            </div>
            
            <DataTable columns={["Name", "Type", "Program", "Term", "Amount", "Due Date"]}>
              {structuresQuery.isLoading ? (
                <Row><Cell className="text-center">Loading...</Cell></Row>
              ) : structuresQuery.data?.length === 0 ? (
                <Row><Cell className="text-center text-muted-foreground">No fee structures configured.</Cell></Row>
              ) : (
                structuresQuery.data?.map((s, i) => (
                  <Row key={s.id} index={i}>
                    <Cell className="font-medium">{s.name}</Cell>
                    <Cell><Badge tone="info">{s.feeType}</Badge></Cell>
                    <Cell>{programsQuery.data?.find((p) => p.id === s.programId)?.name || s.programId}</Cell>
                    <Cell>{termsQuery.data?.find((t) => t.id === s.termId)?.name || s.termId}</Cell>
                    <Cell className="text-right font-medium">{moneyINR(s.amount)}</Cell>
                    <Cell>{shortDate(s.dueDate)}</Cell>
                  </Row>
                ))
              )}
            </DataTable>
          </Card>
        )}

        {activeTab === "invoices" && (
          <Card className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-semibold">Student Invoices</h2>
              <Button variant="accent" onClick={() => setIsBulkInvoiceOpen(true)}>Bulk Generate Invoices</Button>
              <Dialog open={isBulkInvoiceOpen} onClose={() => setIsBulkInvoiceOpen(false)} title="Generate Batch Invoices">
                  <form
                    className="grid gap-4 mt-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      const fd = new FormData(e.currentTarget);
                      const structureId = Number(fd.get("structureId"));
                      if (structureId) generateBulkMutation.mutate(structureId);
                    }}
                  >
                    <Field label="Fee Structure">
                      <Select name="structureId" required>
                        <option value="">Select Structure...</option>
                        {structuresQuery.data?.map((s) => (
                          <option key={s.id} value={s.id}>
                            {s.name} — {moneyINR(s.amount)} due {s.dueDate}
                          </option>
                        ))}
                      </Select>
                    </Field>
                    <div className="flex justify-end gap-2 mt-4">
                      <Button variant="outline" type="button" onClick={() => setIsBulkInvoiceOpen(false)}>Cancel</Button>
                      <Button variant="accent" type="submit" disabled={generateBulkMutation.isPending}>
                        {generateBulkMutation.isPending ? "Generating..." : "Generate"}
                      </Button>
                    </div>
                  </form>
              </Dialog>
            </div>

            <DataTable columns={["Invoice #", "Student ID", "Amount", "Paid", "Status", "Due Date"]}>
              {invoicesQuery.isLoading ? (
                <Row><Cell className="text-center">Loading...</Cell></Row>
              ) : invoicesQuery.data?.length === 0 ? (
                <Row><Cell className="text-center text-muted-foreground">No invoices found.</Cell></Row>
              ) : (
                invoicesQuery.data?.map((inv, i) => (
                  <Row key={inv.id} index={i}>
                    <Cell className="font-mono text-sm">{inv.invoiceNumber}</Cell>
                    <Cell className="font-mono text-sm">{inv.studentId}</Cell>
                    <Cell className="text-right font-medium">{moneyINR(inv.amount)}</Cell>
                    <Cell className="text-right text-muted-foreground">{moneyINR(inv.paidAmount)}</Cell>
                    <Cell>
                      <Badge tone={inv.status === "PAID" ? "success" : inv.status === "PENDING" ? "danger" : "warning"}>
                        {inv.status}
                      </Badge>
                    </Cell>
                    <Cell>{shortDate(inv.dueDate)}</Cell>
                  </Row>
                ))
              )}
            </DataTable>
          </Card>
        )}
      </FadeUp>
    </div>
  );
}

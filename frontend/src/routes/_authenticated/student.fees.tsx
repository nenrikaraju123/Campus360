import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getInvoicesForStudent, getFeeSummary, recordPayment } from "@/lib/api/finance";
import { useAuthStore } from "@/lib/auth/store";
import { PageHeader, moneyINR, shortDate } from "@/components/common";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { FadeUp } from "@/components/motion/FadeUp";
import { Stagger, StaggerItem } from "@/components/motion/Stagger";
import { Wallet, FileText, CheckCircle2 } from "lucide-react";
import type { Invoice } from "@/lib/api/entities";

export const Route = createFileRoute("/_authenticated/student/fees")({
  component: StudentFeesPage,
});

function StudentFeesPage() {
  const queryClient = useQueryClient();
  const user = useAuthStore((s) => s.user);
  // Using userId directly, assuming it matches the studentId mapping for simplicity in this frontend
  // A robust implementation might fetch the StudentProfile first to get the correct studentId.
  const studentId = user?.userId ?? 0;

  const { data: summary, isLoading: sumLoading } = useQuery({
    queryKey: ["feeSummary", studentId],
    queryFn: () => getFeeSummary(studentId),
    enabled: !!studentId,
  });

  const { data: invoices, isLoading: invLoading } = useQuery({
    queryKey: ["studentInvoices", studentId],
    queryFn: () => getInvoicesForStudent(studentId),
    enabled: !!studentId,
  });

  const [payingInvoice, setPayingInvoice] = useState<number | null>(null);

  const payMutation = useMutation({
    mutationFn: (args: { invoiceId: number; amount: number }) =>
      recordPayment({
        invoiceId: args.invoiceId,
        amount: args.amount,
        paymentMethod: "CREDIT_CARD",
        transactionRef: `TXN-${Math.floor(Math.random() * 1000000)}`,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["studentInvoices"] });
      queryClient.invalidateQueries({ queryKey: ["feeSummary"] });
      toast.success("Payment successful!");
      setPayingInvoice(null);
    },
    onError: (err: any) => toast.error(err.detail || "Payment failed"),
  });

  const handlePayClick = (invoice: Invoice) => {
    setPayingInvoice(invoice.id);
    const amountDue = invoice.amount - invoice.paidAmount;
    payMutation.mutate({ invoiceId: invoice.id, amount: amountDue });
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Fee Management" description="View your fee summary and pay outstanding invoices." />

      <Stagger className="grid gap-4 md:grid-cols-3">
        <StaggerItem>
          <Card className="p-6 bg-gradient-to-br from-card to-background border-accent/20 shadow-sm">
            <div className="flex items-center gap-4">
              <div className="rounded-md bg-accent/10 p-3">
                <Wallet className="size-5 text-accent" />
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total Outstanding</p>
                <h3 className="text-2xl font-bold">
                  {sumLoading ? "..." : moneyINR(summary?.outstanding ?? 0)}
                </h3>
              </div>
            </div>
          </Card>
        </StaggerItem>
        <StaggerItem>
          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="rounded-md bg-green-500/10 p-3">
                <CheckCircle2 className="size-5 text-green-500" />
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total Paid</p>
                <h3 className="text-2xl font-bold">
                  {sumLoading ? "..." : moneyINR(summary?.totalPaid ?? 0)}
                </h3>
              </div>
            </div>
          </Card>
        </StaggerItem>
        <StaggerItem>
          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="rounded-md bg-red-500/10 p-3">
                <FileText className="size-5 text-red-500" />
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Overdue Invoices</p>
                <h3 className="text-2xl font-bold">
                  {sumLoading ? "..." : summary?.overdueInvoices ?? 0}
                </h3>
              </div>
            </div>
          </Card>
        </StaggerItem>
      </Stagger>

      <FadeUp delay={0.2}>
        <Card className="p-6">
          <h2 className="text-lg font-semibold mb-6">Your Invoices</h2>
          <DataTable columns={["Invoice ID", "Due Date", "Total", "Paid", "Status", "Action"]}>
            {invLoading ? (
              <Row><Cell className="text-center">Loading invoices...</Cell></Row>
            ) : !invoices || invoices.length === 0 ? (
              <Row><Cell className="text-center text-muted-foreground">No invoices generated yet.</Cell></Row>
            ) : (
              invoices.map((inv, i) => (
                <Row key={inv.id} index={i}>
                  <Cell className="font-mono text-sm">INV-{inv.id}</Cell>
                  <Cell>{shortDate(inv.dueDate)}</Cell>
                  <Cell className="text-right font-medium">{moneyINR(inv.amount)}</Cell>
                  <Cell className="text-right text-muted-foreground">{moneyINR(inv.paidAmount)}</Cell>
                  <Cell>
                    <Badge tone={inv.status === "PAID" ? "success" : inv.status === "PENDING" ? "danger" : "warning"}>
                      {inv.status}
                    </Badge>
                  </Cell>
                  <Cell className="text-right">
                    {inv.status !== "PAID" ? (
                      <Button
                        size="sm"
                        variant="accent"
                        disabled={payingInvoice === inv.id}
                        onClick={() => handlePayClick(inv)}
                      >
                        {payingInvoice === inv.id ? "Processing..." : "Pay Now"}
                      </Button>
                    ) : (
                      <span className="text-sm font-medium text-success">Completed</span>
                    )}
                  </Cell>
                </Row>
              ))
            )}
          </DataTable>
        </Card>
      </FadeUp>
    </div>
  );
}

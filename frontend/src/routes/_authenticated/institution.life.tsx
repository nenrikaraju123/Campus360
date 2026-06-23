import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getAllGrievances, getAllDocumentRequests, updateGrievanceStatus, updateDocumentStatus } from "@/lib/api/studentlife";
import { PageHeader, shortDate } from "@/components/common";
import { Card } from "@/components/ui/card";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { FadeUp } from "@/components/motion/FadeUp";

export const Route = createFileRoute("/_authenticated/institution/life")({
  component: InstitutionLifePage,
});

function InstitutionLifePage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"grievances" | "documents">("grievances");
  const [resolvingGrievance, setResolvingGrievance] = useState<number | null>(null);

  const { data: grievances, isLoading: grievLoading } = useQuery({
    queryKey: ["allGrievances"],
    queryFn: () => getAllGrievances().then((res) => res.content),
  });

  const { data: documents, isLoading: docLoading } = useQuery({
    queryKey: ["allDocuments"],
    queryFn: () => getAllDocumentRequests(),
    enabled: activeTab === "documents",
  });

  const updateGrievMutation = useMutation({
    mutationFn: (args: { id: number; status: string; resolution?: string }) =>
      updateGrievanceStatus(args.id, args.status, args.resolution),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allGrievances"] });
      toast.success("Grievance updated");
      setResolvingGrievance(null);
    },
    onError: (err: any) => toast.error(err.detail || "Update failed"),
  });

  const updateDocMutation = useMutation({
    mutationFn: (args: { id: number; status: string }) => updateDocumentStatus(args.id, args.status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allDocuments"] });
      toast.success("Document request updated");
    },
    onError: (err: any) => toast.error(err.detail || "Update failed"),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Student Life Administration"
        description="Manage student grievances and document requests."
        actions={
          <div className="flex gap-2 bg-muted/50 p-1 rounded-lg">
            <button
              onClick={() => setActiveTab("grievances")}
              className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${
                activeTab === "grievances" ? "bg-background shadow text-foreground" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Grievances
            </button>
            <button
              onClick={() => setActiveTab("documents")}
              className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${
                activeTab === "documents" ? "bg-background shadow text-foreground" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Document Requests
            </button>
          </div>
        }
      />

      <FadeUp>
        {activeTab === "grievances" && (
          <Card className="p-6">
            <h2 className="text-lg font-semibold mb-6">Open Grievances</h2>
            <DataTable columns={["Student ID", "Category", "Subject", "Priority", "Status", "Action"]}>
              {grievLoading ? (
                <Row><Cell className="text-center">Loading...</Cell></Row>
              ) : !grievances || grievances.length === 0 ? (
                <Row><Cell className="text-center text-muted-foreground">No grievances to display.</Cell></Row>
              ) : (
                grievances.map((g, i) => (
                  <Row key={g.id} index={i}>
                    <Cell className="font-mono text-sm">{g.studentId}</Cell>
                    <Cell><Badge tone="neutral">{g.category}</Badge></Cell>
                    <Cell>
                      <div className="font-medium">{g.subject}</div>
                      <div className="text-xs text-muted-foreground mt-1 truncate max-w-[200px]">{g.description}</div>
                    </Cell>
                    <Cell>
                      <Badge tone={g.priority === "HIGH" ? "danger" : "neutral"}>
                        {g.priority}
                      </Badge>
                    </Cell>
                    <Cell>
                      <Badge tone={g.status === "RESOLVED" ? "success" : g.status === "OPEN" ? "danger" : "warning"}>
                        {g.status}
                      </Badge>
                    </Cell>
                    <Cell>
                      {resolvingGrievance === g.id ? (
                        <div className="flex gap-2 items-center">
                          <Input id={`res-${g.id}`} placeholder="Resolution note..." className="h-8 w-[150px]" />
                          <Button size="sm" variant="accent" onClick={() => {
                            const res = (document.getElementById(`res-${g.id}`) as HTMLInputElement).value;
                            updateGrievMutation.mutate({ id: g.id, status: "RESOLVED", resolution: res });
                          }}>Resolve</Button>
                          <Button size="sm" variant="outline" onClick={() => setResolvingGrievance(null)}>Cancel</Button>
                        </div>
                      ) : g.status !== "RESOLVED" ? (
                        <div className="flex gap-2">
                          {g.status === "OPEN" && (
                            <Button size="sm" variant="outline" onClick={() => updateGrievMutation.mutate({ id: g.id, status: "IN_PROGRESS" })}>
                              Mark In Progress
                            </Button>
                          )}
                          <Button size="sm" variant="accent" onClick={() => setResolvingGrievance(g.id)}>
                            Resolve
                          </Button>
                        </div>
                      ) : (
                        <span className="text-xs text-muted-foreground">{g.resolution}</span>
                      )}
                    </Cell>
                  </Row>
                ))
              )}
            </DataTable>
          </Card>
        )}

        {activeTab === "documents" && (
          <Card className="p-6">
            <h2 className="text-lg font-semibold mb-6">Document Requests</h2>
            <DataTable columns={["Student ID", "Type", "Purpose", "Date", "Status", "Action"]}>
              {docLoading ? (
                <Row><Cell className="text-center">Loading...</Cell></Row>
              ) : !documents || documents.length === 0 ? (
                <Row><Cell className="text-center text-muted-foreground">No document requests found.</Cell></Row>
              ) : (
                documents.map((d, i) => (
                  <Row key={d.id} index={i}>
                    <Cell className="font-mono text-sm">{d.studentId}</Cell>
                    <Cell className="font-medium">{d.type}</Cell>
                    <Cell className="text-muted-foreground">{d.purpose}</Cell>
                    <Cell>{shortDate(d.createdAt!)}</Cell>
                    <Cell>
                      <Badge tone={d.status === "READY" || d.status === "COLLECTED" ? "success" : "warning"}>
                        {d.status}
                      </Badge>
                    </Cell>
                    <Cell>
                      <Select
                        value={d.status}
                        onChange={(e) => updateDocMutation.mutate({ id: d.id, status: e.target.value })}
                        disabled={updateDocMutation.isPending}
                      >
                        <option value="PENDING">PENDING</option>
                        <option value="PROCESSING">PROCESSING</option>
                        <option value="READY">READY</option>
                        <option value="COLLECTED">COLLECTED</option>
                      </Select>
                    </Cell>
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

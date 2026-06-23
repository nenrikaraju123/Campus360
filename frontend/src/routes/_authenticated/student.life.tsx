import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getMyGrievances, createGrievance, getMyDocumentRequests, createDocumentRequest } from "@/lib/api/studentlife";
import { PageHeader, shortDate } from "@/components/common";
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

export const Route = createFileRoute("/_authenticated/student/life")({
  component: StudentLifePage,
});

function StudentLifePage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"grievances" | "documents">("grievances");
  const [isGrievanceOpen, setIsGrievanceOpen] = useState(false);
  const [isDocOpen, setIsDocOpen] = useState(false);

  const { data: grievances, isLoading: grievLoading } = useQuery({
    queryKey: ["myGrievances"],
    queryFn: () => getMyGrievances(),
  });

  const { data: documents, isLoading: docLoading } = useQuery({
    queryKey: ["myDocuments"],
    queryFn: () => getMyDocumentRequests(),
    enabled: activeTab === "documents",
  });

  const createGrievMutation = useMutation({
    mutationFn: createGrievance,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["myGrievances"] });
      toast.success("Grievance submitted successfully");
      setIsGrievanceOpen(false);
    },
    onError: (err: any) => toast.error(err.detail || "Submission failed"),
  });

  const createDocMutation = useMutation({
    mutationFn: createDocumentRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["myDocuments"] });
      toast.success("Document request submitted successfully");
      setIsDocOpen(false);
    },
    onError: (err: any) => toast.error(err.detail || "Submission failed"),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Student Life Support"
        description="Raise grievances or request official documents from the administration."
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
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-semibold">My Grievances</h2>
              <Button variant="accent" onClick={() => setIsGrievanceOpen(true)}>Report Issue</Button>
              <Dialog open={isGrievanceOpen} onClose={() => setIsGrievanceOpen(false)} title="Submit a Grievance">
                  <form
                    className="grid gap-4 mt-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      const fd = new FormData(e.currentTarget);
                      createGrievMutation.mutate({
                        category: fd.get("category") as any,
                        subject: fd.get("subject") as string,
                        description: fd.get("description") as string,
                        priority: fd.get("priority") as any,
                      });
                    }}
                  >
                    <div className="grid grid-cols-2 gap-4">
                      <Field label="Category">
                        <Select name="category" required>
                          <option value="ACADEMIC">Academic</option>
                          <option value="HOSTEL">Hostel</option>
                          <option value="FACILITY">Facility</option>
                          <option value="FINANCE">Finance</option>
                          <option value="OTHER">Other</option>
                        </Select>
                      </Field>
                      <Field label="Priority">
                        <Select name="priority" required>
                          <option value="LOW">Low</option>
                          <option value="MEDIUM">Medium</option>
                          <option value="HIGH">High</option>
                        </Select>
                      </Field>
                    </div>
                    <Field label="Subject">
                      <Input name="subject" required placeholder="Brief summary" />
                    </Field>
                    <Field label="Description">
                      <textarea
                        name="description"
                        required
                        rows={4}
                        className="w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                        placeholder="Provide detailed information..."
                      />
                    </Field>
                    <div className="flex justify-end gap-2 mt-4">
                      <Button variant="outline" type="button" onClick={() => setIsGrievanceOpen(false)}>Cancel</Button>
                      <Button variant="accent" type="submit" disabled={createGrievMutation.isPending}>
                        Submit
                      </Button>
                    </div>
                  </form>
              </Dialog>
            </div>
            
            <DataTable columns={["Date", "Category", "Subject", "Status", "Resolution"]}>
              {grievLoading ? (
                <Row><Cell className="text-center">Loading...</Cell></Row>
              ) : !grievances || grievances.length === 0 ? (
                <Row><Cell className="text-center text-muted-foreground">No grievances submitted.</Cell></Row>
              ) : (
                grievances.map((g, i) => (
                  <Row key={g.id} index={i}>
                    <Cell>{shortDate(g.createdAt!)}</Cell>
                    <Cell><Badge tone="neutral">{g.category}</Badge></Cell>
                    <Cell className="font-medium">{g.subject}</Cell>
                    <Cell>
                      <Badge tone={g.status === "RESOLVED" ? "success" : g.status === "OPEN" ? "danger" : "warning"}>
                        {g.status}
                      </Badge>
                    </Cell>
                    <Cell className="text-sm text-muted-foreground">{g.resolution || "—"}</Cell>
                  </Row>
                ))
              )}
            </DataTable>
          </Card>
        )}

        {activeTab === "documents" && (
          <Card className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-semibold">Document Requests</h2>
              <Button variant="accent" onClick={() => setIsDocOpen(true)}>Request Document</Button>
              <Dialog open={isDocOpen} onClose={() => setIsDocOpen(false)} title="Request Official Document">
                  <form
                    className="grid gap-4 mt-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      const fd = new FormData(e.currentTarget);
                      createDocMutation.mutate({
                        type: fd.get("type") as any,
                        purpose: fd.get("purpose") as string,
                      });
                    }}
                  >
                    <Field label="Document Type">
                      <Select name="type" required>
                        <option value="TRANSCRIPT">Official Transcript</option>
                        <option value="BONAFIDE">Bonafide Certificate</option>
                        <option value="LEAVING_CERTIFICATE">Leaving Certificate</option>
                        <option value="OTHER">Other</option>
                      </Select>
                    </Field>
                    <Field label="Purpose">
                      <Input name="purpose" required placeholder="e.g. Higher Education Application" />
                    </Field>
                    <div className="flex justify-end gap-2 mt-4">
                      <Button variant="outline" type="button" onClick={() => setIsDocOpen(false)}>Cancel</Button>
                      <Button variant="accent" type="submit" disabled={createDocMutation.isPending}>
                        Request
                      </Button>
                    </div>
                  </form>
              </Dialog>
            </div>

            <DataTable columns={["Date", "Document Type", "Purpose", "Status"]}>
              {docLoading ? (
                <Row><Cell className="text-center">Loading...</Cell></Row>
              ) : !documents || documents.length === 0 ? (
                <Row><Cell className="text-center text-muted-foreground">No document requests found.</Cell></Row>
              ) : (
                documents.map((d, i) => (
                  <Row key={d.id} index={i}>
                    <Cell>{shortDate(d.createdAt!)}</Cell>
                    <Cell className="font-medium">{d.type}</Cell>
                    <Cell className="text-muted-foreground">{d.purpose}</Cell>
                    <Cell>
                      <Badge tone={d.status === "READY" || d.status === "COLLECTED" ? "success" : "warning"}>
                        {d.status}
                      </Badge>
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

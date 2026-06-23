import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { usersApi, type User } from "@/lib/api/users";
import { PageHeader, shortDate } from "@/components/common";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Dialog } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { FadeUp } from "@/components/motion/FadeUp";
import { ImportWizard } from "@/components/ui/import-wizard";
import { UserPlus, Upload, ShieldCheck } from "lucide-react";

export const Route = createFileRoute("/_authenticated/institution/users")({
  component: UsersPage,
});

function UsersPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [isBulkOpen, setIsBulkOpen] = useState(false);

  const { data: usersData, isLoading } = useQuery({
    queryKey: ["institutionUsers", page],
    queryFn: () => usersApi.listUsers(page, 20),
  });

  const bulkCreateMutation = useMutation({
    mutationFn: usersApi.bulkCreateUsers,
    onSuccess: (results) => {
      queryClient.invalidateQueries({ queryKey: ["institutionUsers"] });
      const successCount = results.filter((r) => r.success).length;
      const failCount = results.length - successCount;
      if (failCount === 0) {
        toast.success(`Successfully created ${successCount} users.`);
      } else {
        toast.warning(`Created ${successCount} users, but ${failCount} failed.`);
      }
      setIsBulkOpen(false);
    },
    onError: (err: any) => {
      toast.error(err.detail || "Bulk import failed completely.");
    },
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Users & Roles"
        description="Manage institution staff, faculty, administrators, and their access permissions."
        actions={
          <div className="flex gap-3">
            <Button variant="outline" onClick={() => setIsBulkOpen(true)}>
              <Upload className="mr-2 size-4" /> Bulk Import
            </Button>
            <Button variant="accent">
              <UserPlus className="mr-2 size-4" /> Add User
            </Button>
          </div>
        }
      />

      <FadeUp>
        <Card className="p-0">
          <DataTable columns={["Name", "Email", "Roles", "Status", "Joined"]}>
            {isLoading ? (
              <Row><Cell colSpan={5} className="text-center py-8">Loading users...</Cell></Row>
            ) : !usersData?.content || usersData.content.length === 0 ? (
              <Row><Cell colSpan={5} className="text-center py-8 text-muted-foreground">No users found.</Cell></Row>
            ) : (
              usersData.content.map((u, i) => (
                <Row key={u.id} index={i}>
                  <Cell className="font-medium">{u.fullName}</Cell>
                  <Cell className="text-muted-foreground">{u.email}</Cell>
                  <Cell>
                    <div className="flex flex-wrap gap-1">
                      {u.roles.map((r) => (
                        <Badge key={r.id} tone="neutral" size="sm" className="bg-accent/10 text-accent border-accent/20">
                          {r.name}
                        </Badge>
                      ))}
                    </div>
                  </Cell>
                  <Cell>
                    <Badge tone={u.status === "ACTIVE" ? "success" : "danger"}>
                      {u.status}
                    </Badge>
                  </Cell>
                  <Cell className="text-sm text-muted-foreground">{shortDate(u.createdAt)}</Cell>
                </Row>
              ))
            )}
          </DataTable>
          {usersData && usersData.totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-border p-4">
              <span className="text-sm text-muted-foreground">
                Page {page + 1} of {usersData.totalPages} ({usersData.totalElements} total)
              </span>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
                  Previous
                </Button>
                <Button variant="outline" size="sm" disabled={page >= usersData.totalPages - 1} onClick={() => setPage(page + 1)}>
                  Next
                </Button>
              </div>
            </div>
          )}
        </Card>
      </FadeUp>

      {/* Bulk Import Wizard Dialog */}
      <Dialog open={isBulkOpen} onClose={() => setIsBulkOpen(false)} title="Bulk Create Users" maxWidth="3xl">
        <ImportWizard
          type="USERS"
          expectedColumns={["fullName", "email", "roles"]}
          onDownloadTemplate={() => {
            const csv = "fullName,email,roles\nJohn Doe,john@example.com,FACULTY\nJane Smith,jane@example.com,INSTITUTION_ADMIN";
            const blob = new Blob([csv], { type: "text/csv" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "users_template.csv";
            a.click();
          }}
          onCommit={async (validRows) => {
            const users = validRows.map(row => ({
              fullName: row.fullName,
              email: row.email,
              roles: row.roles ? row.roles.split(",").map((r: string) => r.trim()) : [],
            }));
            await bulkCreateMutation.mutateAsync({ users });
          }}
          onCancel={() => setIsBulkOpen(false)}
        />
      </Dialog>
    </div>
  );
}

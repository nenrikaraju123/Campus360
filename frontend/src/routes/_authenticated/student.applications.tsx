import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, Loader2, XCircle } from "lucide-react";
import { toast } from "sonner";
import { ApiError } from "@/lib/api/client";
import { listPostings, myApplications, myOffers, respondToOffer } from "@/lib/api/placement";
import { getMyProfile } from "@/lib/api/students";
import { DataState, PageHeader, moneyINR, shortDate } from "@/components/common";
import { StatusBadge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Cell, DataTable, Row } from "@/components/ui/table";

export const Route = createFileRoute("/_authenticated/student/applications")({
  component: ApplicationsPage,
});

function ApplicationsPage() {
  const qc = useQueryClient();
  const me = useQuery({ queryKey: ["myProfile"], queryFn: getMyProfile });
  const applications = useQuery({ queryKey: ["myApplications"], queryFn: myApplications });
  const offers = useQuery({ queryKey: ["myOffers"], queryFn: myOffers });
  const postings = useQuery({ queryKey: ["postings", "all"], queryFn: () => listPostings(false) });

  const respond = useMutation({
    mutationFn: ({ offerId, decision }: { offerId: number; decision: "ACCEPT" | "DECLINE" }) =>
      respondToOffer(offerId, decision),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["myOffers"] });
      toast.success("Offer response saved");
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Unable to respond to offer"),
  });

  const apps = applications.data ?? [];
  const offerRows = offers.data ?? [];
  const titleForPosting = (postingId: number) =>
    postings.data?.find((posting) => posting.id === postingId)?.title ?? `Posting #${postingId}`;
  const applicationForOffer = (applicationId: number) =>
    apps.find((application) => application.id === applicationId);
  const titleForOffer = (applicationId: number) => {
    const application = applicationForOffer(applicationId);
    return application ? titleForPosting(application.postingId) : `Application #${applicationId}`;
  };

  return (
    <div>
      <PageHeader
        title="Applications"
        description={
          me.data
            ? `Tracking applications for ${me.data.rollNumber}`
            : "Track application status and placement offers."
        }
      />

      <section>
        <h2 className="mb-3 text-sm font-medium text-muted-foreground">My applications</h2>
        <DataState
          isLoading={applications.isLoading || postings.isLoading}
          error={applications.error ?? postings.error}
          isEmpty={apps.length === 0}
          emptyTitle="No applications yet"
          emptyBody="Apply to an opportunity to start tracking it here."
        >
          <DataTable columns={["Posting", "Status", "Applied", "Updated"]}>
            {apps.map((application, index) => (
              <Row key={application.id} index={index}>
                <Cell className="font-medium">{titleForPosting(application.postingId)}</Cell>
                <Cell><StatusBadge status={application.status} /></Cell>
                <Cell className="whitespace-nowrap text-xs text-muted-foreground">
                  {shortDate(application.appliedAt)}
                </Cell>
                <Cell className="whitespace-nowrap text-xs text-muted-foreground">
                  {shortDate(application.updatedAt)}
                </Cell>
              </Row>
            ))}
          </DataTable>
        </DataState>
      </section>

      <section className="mt-8">
        <h2 className="mb-3 text-sm font-medium text-muted-foreground">My offers</h2>
        <DataState
          isLoading={offers.isLoading || applications.isLoading || postings.isLoading}
          error={offers.error ?? applications.error ?? postings.error}
          isEmpty={offerRows.length === 0}
          emptyTitle="No offers yet"
          emptyBody="Offers extended by the placement cell will appear here."
        >
          <div className="grid gap-4 lg:grid-cols-2">
            {offerRows.map((offer) => {
              const isOpen = offer.status === "EXTENDED" || offer.status === "OFFERED";
              return (
                <Card key={offer.id} className="transition-colors hover:border-accent/40">
                  <CardContent className="p-6">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                      <div>
                        <h3 className="font-semibold">{titleForOffer(offer.applicationId)}</h3>
                        <p className="mt-1 text-sm text-muted-foreground">
                          Joining {shortDate(offer.joiningDate)}
                        </p>
                      </div>
                      <StatusBadge status={offer.status} />
                    </div>

                    <div className="mt-5 grid gap-4 sm:grid-cols-2">
                      <div>
                        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">CTC</p>
                        <p className="mt-1 font-mono text-xl font-semibold">{moneyINR(offer.ctc)}</p>
                      </div>
                      <div>
                        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Created</p>
                        <p className="mt-1 font-mono text-sm">{shortDate(offer.createdAt)}</p>
                      </div>
                    </div>

                    {isOpen && (
                      <div className="mt-5 flex justify-end gap-2">
                        <Button
                          type="button"
                          variant="outline"
                          disabled={respond.isPending}
                          onClick={() => respond.mutate({ offerId: offer.id, decision: "DECLINE" })}
                        >
                          {respond.isPending ? <Loader2 className="size-4 animate-spin" /> : <XCircle className="size-4" />}
                          Decline
                        </Button>
                        <Button
                          type="button"
                          variant="accent"
                          disabled={respond.isPending}
                          onClick={() => respond.mutate({ offerId: offer.id, decision: "ACCEPT" })}
                        >
                          {respond.isPending ? <Loader2 className="size-4 animate-spin" /> : <CheckCircle2 className="size-4" />}
                          Accept
                        </Button>
                      </div>
                    )}
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </DataState>
      </section>
    </div>
  );
}

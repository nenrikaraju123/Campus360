import { create } from "zustand";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { API_BASE } from "@/lib/api/client";
import { useAuthStore } from "@/lib/auth/store";
import type { NotificationMessage } from "@/lib/api/entities";

interface StoredNotification extends NotificationMessage {
  id: string;
}

interface NotificationState {
  items: StoredNotification[];
  unread: number;
  push: (n: NotificationMessage) => void;
  markAllRead: () => void;
  clear: () => void;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  items: [],
  unread: 0,
  push: (n) =>
    set((s) => ({
      items: [{ ...n, id: crypto.randomUUID() }, ...s.items].slice(0, 50),
      unread: s.unread + 1,
    })),
  markAllRead: () => set({ unread: 0 }),
  clear: () => set({ items: [], unread: 0 }),
}));

const KNOWN_EVENTS = new Set(["JOB_POSTED", "OFFER_EXTENDED"]);

class FatalError extends Error {}

/**
 * Subscribe to the tenant's SSE stream. Uses fetch (not EventSource) so we can
 * attach the Authorization header. Returns an abort function.
 */
export function subscribeNotifications(onMessage: (n: NotificationMessage) => void): () => void {
  const controller = new AbortController();
  const token = useAuthStore.getState().accessToken;
  if (!token) return () => {};

  fetchEventSource(`${API_BASE}/api/v1/notifications/stream`, {
    headers: { Authorization: `Bearer ${token}` },
    signal: controller.signal,
    openWhenHidden: true,
    async onopen(res) {
      if (res.status === 401 || res.status === 403) {
        throw new FatalError("unauthorized");
      }
    },
    onmessage(ev) {
      if (KNOWN_EVENTS.has(ev.event)) {
        try {
          onMessage(JSON.parse(ev.data) as NotificationMessage);
        } catch {
          /* ignore malformed */
        }
      }
    },
    onerror(err) {
      if (err instanceof FatalError) throw err; // stop retrying
      // otherwise let the library back off and retry
    },
  }).catch(() => {
    /* stream closed */
  });

  return () => controller.abort();
}

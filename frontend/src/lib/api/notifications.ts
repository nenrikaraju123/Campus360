import { apiRequest } from "./client";
import type { AppNotification } from "./entities";

export const getNotifications = () =>
  apiRequest<{ content: AppNotification[] }>("/api/v1/notifications/inbox").then(res => res.content);

export const markNotificationRead = (id: number) =>
  apiRequest<void>(`/api/v1/notifications/${id}/read`, { method: "POST" });

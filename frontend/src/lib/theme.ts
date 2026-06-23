import { create } from "zustand";

type Theme = "light" | "dark";
const KEY = "campus360.theme";

function apply(theme: Theme) {
  document.documentElement.classList.toggle("dark", theme === "dark");
}

function initialTheme(): Theme {
  if (typeof window === "undefined") return "light";
  const stored = localStorage.getItem(KEY) as Theme | null;
  if (stored) return stored;
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

const start = initialTheme();
if (typeof document !== "undefined") apply(start);

interface ThemeState {
  theme: Theme;
  toggle: () => void;
}

export const useTheme = create<ThemeState>((set, get) => ({
  theme: start,
  toggle: () => {
    const next: Theme = get().theme === "dark" ? "light" : "dark";
    localStorage.setItem(KEY, next);
    apply(next);
    set({ theme: next });
  },
}));

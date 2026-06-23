import { Link } from "@tanstack/react-router";
import { Brand } from "./Brand";
import { ThemeToggle } from "./ThemeToggle";
import { Button } from "@/components/ui/button";

export function PublicNav() {
  return (
    <header className="sticky top-0 z-40 w-full border-b border-border/70 bg-background/70 backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <Brand />
        <nav className="hidden items-center gap-1 md:flex">
          <a
            href="/#features"
            className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            Features
          </a>
          <Link
            to="/demo"
            className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            Demo
          </Link>
          <Link
            to="/login"
            className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
          >
            Sign in
          </Link>
        </nav>
        <div className="flex items-center gap-2">
          <ThemeToggle />
          <Link to="/register" className="hidden sm:block">
            <Button variant="accent" size="sm">
              Register institution
            </Button>
          </Link>
        </div>
      </div>
    </header>
  );
}

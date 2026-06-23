import React, { useState } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";

interface FilterBarProps {
  onSearchChange?: (value: string) => void;
  searchPlaceholder?: string;
  actions?: React.ReactNode;
  filters?: React.ReactNode;
}

export function FilterBar({
  onSearchChange,
  searchPlaceholder = "Search...",
  actions,
  filters,
}: FilterBarProps) {
  const [searchValue, setSearchValue] = useState("");

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setSearchValue(val);
    if (onSearchChange) {
      onSearchChange(val);
    }
  };

  return (
    <div className="flex flex-col gap-3 pb-4 pt-2 md:flex-row md:items-center md:justify-between">
      <div className="flex flex-1 items-center gap-3">
        {onSearchChange && (
          <div className="relative w-full md:max-w-sm">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={searchValue}
              onChange={handleSearch}
              placeholder={searchPlaceholder}
              className="pl-9"
            />
          </div>
        )}
        {filters && <div className="flex items-center gap-2">{filters}</div>}
      </div>
      {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
    </div>
  );
}

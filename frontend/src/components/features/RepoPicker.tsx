import React, { useState } from "react";
import { GitHubRepository } from "@/types";
import { Search, FolderGit2, Lock, Globe, Check, GitBranch } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface RepoPickerProps {
  repositories: GitHubRepository[];
  selectedRepo: GitHubRepository | null;
  onSelectRepo: (repo: GitHubRepository) => void;
  isLoading?: boolean;
}

export function RepoPicker({
  repositories,
  selectedRepo,
  onSelectRepo,
  isLoading = false,
}: RepoPickerProps) {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredRepos = repositories.filter(
    (repo) =>
      repo.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      repo.fullName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-4">
      {/* Search Input Filter */}
      <div className="relative">
        <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Search repositories..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-9 bg-card/60 border-border/80 text-xs"
        />
      </div>

      {/* Repositories List */}
      <div className="border border-border/80 rounded-xl overflow-hidden divide-y divide-border/60 bg-card/40 max-h-[360px] overflow-y-auto">
        {isLoading ? (
          <div className="p-8 text-center text-xs text-muted-foreground">
            Loading GitHub repositories...
          </div>
        ) : filteredRepos.length === 0 ? (
          <div className="p-8 text-center text-xs text-muted-foreground">
            No matching repositories found.
          </div>
        ) : (
          filteredRepos.map((repo) => {
            const isSelected = selectedRepo?.id === repo.id;
            return (
              <div
                key={repo.id}
                onClick={() => onSelectRepo(repo)}
                className={cn(
                  "p-3.5 flex items-center justify-between hover:bg-white/[0.04] transition-colors cursor-pointer group",
                  isSelected && "bg-white/[0.07] border-l-2 border-l-white"
                )}
              >
                <div className="flex items-center gap-3 min-w-0">
                  <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-white/10 flex items-center justify-center text-zinc-400 group-hover:text-white flex-shrink-0">
                    <FolderGit2 className="h-4 w-4" />
                  </div>
                  <div className="flex flex-col min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-xs text-foreground truncate">
                        {repo.name}
                      </span>
                      {repo.private ? (
                        <Badge variant="outline" className="text-[9px] px-1 py-0 h-4 gap-1 text-zinc-400 border-zinc-700">
                          <Lock className="h-2.5 w-2.5" />
                          Private
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="text-[9px] px-1 py-0 h-4 gap-1 text-zinc-400 border-zinc-700">
                          <Globe className="h-2.5 w-2.5" />
                          Public
                        </Badge>
                      )}
                    </div>
                    <span className="text-[11px] font-mono text-muted-foreground truncate">
                      {repo.fullName}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-3 flex-shrink-0">
                  <div className="flex items-center gap-1 text-[11px] font-mono text-zinc-500">
                    <GitBranch className="h-3 w-3" />
                    <span>{repo.defaultBranch}</span>
                  </div>
                  <Button
                    size="sm"
                    variant={isSelected ? "default" : "outline"}
                    className="h-7 px-3 text-xs"
                  >
                    {isSelected ? (
                      <span className="flex items-center gap-1">
                        <Check className="h-3 w-3" />
                        Selected
                      </span>
                    ) : (
                      "Import"
                    )}
                  </Button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

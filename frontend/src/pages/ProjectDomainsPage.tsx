import React, { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useProjects } from "@/lib/hooks/useProjects";
import { DomainStatusCard } from "@/components/features/DomainStatusCard";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Globe, Plus, ArrowLeft, ShieldCheck } from "lucide-react";

export function ProjectDomainsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const { useProject, useDomains, addDomainMutation, verifyDomainMutation, deleteDomainMutation } =
    useProjects();

  const { data: project } = useProject(projectId);
  const { data: domains = [], isLoading } = useDomains(projectId);
  const [newDomain, setNewDomain] = useState("");

  const handleAddDomain = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDomain.trim() || !project) return;
    await addDomainMutation.mutateAsync({
      projectId: project.id,
      domainName: newDomain.trim(),
    });
    setNewDomain("");
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-border/80">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Custom Domains</h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Configure custom hostnames, Route53 DNS validation, and SSL certificates
          </p>
        </div>
      </div>

      {/* Add Domain Form Card */}
      <Card className="border-border/80 bg-card/60 p-6 space-y-6">
        <CardHeader className="p-0">
          <CardTitle className="text-base flex items-center gap-2">
            <Globe className="h-4 w-4 text-muted-foreground" />
            <span>Connect a Domain</span>
          </CardTitle>
          <CardDescription className="text-xs">
            Enter the apex domain or subdomain you want to route to this project
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleAddDomain} className="flex gap-3 max-w-lg">
          <Input
            type="text"
            placeholder="api.yourdomain.com or yourdomain.com"
            value={newDomain}
            onChange={(e) => setNewDomain(e.target.value)}
            className="h-9 font-mono text-xs"
            required
          />
          <Button
            type="submit"
            size="sm"
            disabled={addDomainMutation.isPending}
            className="text-xs gap-1.5 flex-shrink-0"
          >
            <Plus className="h-4 w-4" />
            <span>Add Domain</span>
          </Button>
        </form>
      </Card>

      {/* Domain Cards List */}
      <div className="space-y-4">
        {isLoading ? (
          <div className="h-32 bg-card/40 rounded-xl animate-pulse" />
        ) : domains.length === 0 ? (
          <div className="p-8 rounded-xl border border-dashed border-border/80 bg-card/20 text-center text-xs text-muted-foreground">
            No custom domains configured yet. Default subdomain is active.
          </div>
        ) : (
          domains.map((dom) => (
            <DomainStatusCard
              key={dom.id}
              domain={dom}
              onVerify={(id) => verifyDomainMutation.mutate(id)}
              onDelete={(id) => deleteDomainMutation.mutate(id)}
              isVerifying={verifyDomainMutation.isPending}
            />
          ))
        )}
      </div>
    </div>
  );
}

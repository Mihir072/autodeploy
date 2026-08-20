import React from "react";
import { DomainRecord } from "@/types";
import { Globe, ShieldCheck, AlertCircle, Trash2, RefreshCw, Copy, Check } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

interface DomainStatusCardProps {
  domain: DomainRecord;
  onVerify?: (id: string) => void;
  onDelete?: (id: string) => void;
  isVerifying?: boolean;
}

export function DomainStatusCard({
  domain,
  onVerify,
  onDelete,
  isVerifying = false,
}: DomainStatusCardProps) {
  const [copiedKey, setCopiedKey] = React.useState<string | null>(null);

  const copyToClipboard = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const isVerified = domain.status === "VERIFIED";

  return (
    <div className="rounded-xl border border-border/80 bg-card/60 p-6 space-y-6">
      {/* Top Domain Title & Status */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-lg bg-zinc-900 border border-white/10 flex items-center justify-center text-zinc-300">
            <Globe className="h-5 w-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-base text-foreground font-mono">{domain.domainName}</h3>
              {isVerified ? (
                <Badge variant="live" className="text-[10px] gap-1">
                  <ShieldCheck className="h-3 w-3" />
                  SSL Active & Verified
                </Badge>
              ) : (
                <Badge variant="building" className="text-[10px] gap-1">
                  <AlertCircle className="h-3 w-3" />
                  Pending DNS Verification
                </Badge>
              )}
            </div>
            <p className="text-xs text-muted-foreground mt-0.5">
              Auto-provisioned Let's Encrypt SSL certificate attached.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {!isVerified && onVerify && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onVerify(domain.id)}
              disabled={isVerifying}
              className="h-8 text-xs gap-1.5"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${isVerifying ? "animate-spin" : ""}`} />
              <span>Verify DNS</span>
            </Button>
          )}

          {onDelete && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDelete(domain.id)}
              className="h-8 px-2 text-zinc-500 hover:text-red-400"
              title="Delete Domain"
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          )}
        </div>
      </div>

      {/* DNS Configuration Table */}
      {!isVerified && (
        <div className="space-y-3 pt-2">
          <h4 className="text-xs font-mono uppercase tracking-wider text-muted-foreground">
            Required DNS Records (Add in your Domain Registrar)
          </h4>
          <div className="rounded-lg border border-border/80 bg-background/50 overflow-hidden text-xs font-mono">
            <table className="w-full text-left">
              <thead className="bg-zinc-900/80 border-b border-border/80 text-zinc-400 text-[11px]">
                <tr>
                  <th className="p-2.5">Type</th>
                  <th className="p-2.5">Name / Host</th>
                  <th className="p-2.5">Value / Target</th>
                  <th className="p-2.5 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 text-zinc-300">
                <tr>
                  <td className="p-2.5 font-bold text-amber-400">CNAME</td>
                  <td className="p-2.5">@ or www</td>
                  <td className="p-2.5 text-zinc-400">
                    {domain.cnameTarget || "cname.autodeploy.app"}
                  </td>
                  <td className="p-2.5 text-right">
                    <button
                      onClick={() => copyToClipboard(domain.cnameTarget || "cname.autodeploy.app", "cname")}
                      className="text-muted-foreground hover:text-white inline-flex items-center gap-1 text-[11px]"
                    >
                      {copiedKey === "cname" ? <Check className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
                    </button>
                  </td>
                </tr>
                {domain.verificationToken && (
                  <tr>
                    <td className="p-2.5 font-bold text-blue-400">TXT</td>
                    <td className="p-2.5">_autodeploy-challenge</td>
                    <td className="p-2.5 text-zinc-400 truncate max-w-[200px]">
                      {domain.verificationToken}
                    </td>
                    <td className="p-2.5 text-right">
                      <button
                        onClick={() => copyToClipboard(domain.verificationToken!, "txt")}
                        className="text-muted-foreground hover:text-white inline-flex items-center gap-1 text-[11px]"
                      >
                        {copiedKey === "txt" ? <Check className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
                      </button>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

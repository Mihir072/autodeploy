import React, { useEffect, useRef, useState } from "react";
import { Terminal, Copy, Check, ArrowDown, Download, Trash2, Radio } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface LiveLogViewerProps {
  logs: string[];
  isConnected?: boolean;
  isCompleted?: boolean;
  title?: string;
  onClear?: () => void;
  className?: string;
}

export function LiveLogViewer({
  logs,
  isConnected = true,
  isCompleted = false,
  title = "Build & Deployment Execution Logs",
  onClear,
  className,
}: LiveLogViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [autoScroll, setAutoScroll] = useState(true);
  const [copied, setCopied] = useState(false);

  // Auto-scroll when new log lines arrive
  useEffect(() => {
    if (autoScroll && containerRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
    }
  }, [logs, autoScroll]);

  const handleScroll = () => {
    if (!containerRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
    const isAtBottom = scrollHeight - scrollTop - clientHeight < 40;
    setAutoScroll(isAtBottom);
  };

  const handleCopyLogs = () => {
    navigator.clipboard.writeText(logs.join("\n"));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadLogs = () => {
    const element = document.createElement("a");
    const file = new Blob([logs.join("\n")], { type: "text/plain" });
    element.href = URL.createObjectURL(file);
    element.download = `build-logs-${new Date().toISOString().slice(0, 19)}.log`;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  };

  const renderLogLine = (line: string, index: number) => {
    let levelClass = "text-zinc-400";
    if (line.includes("[ERROR]") || line.includes("failed") || line.includes("Error")) {
      levelClass = "text-red-400 font-semibold";
    } else if (line.includes("[WARN]")) {
      levelClass = "text-amber-400";
    } else if (line.includes("✓") || line.includes("successful") || line.includes("Ready")) {
      levelClass = "text-emerald-400 font-semibold";
    } else if (line.includes("pushing") || line.includes("Executing") || line.includes("Layer")) {
      levelClass = "text-blue-300";
    }

    return (
      <div
        key={index}
        className="flex items-start gap-3 py-0.5 hover:bg-white/[0.03] px-2 rounded -mx-2 transition-colors animate-fade-in"
      >
        <span className="text-[11px] font-mono text-zinc-600 select-none w-8 text-right flex-shrink-0">
          {index + 1}
        </span>
        <span className={cn("font-mono text-xs leading-relaxed break-all", levelClass)}>
          {line}
        </span>
      </div>
    );
  };

  return (
    <div
      className={cn(
        "rounded-xl border border-border/80 bg-[#0c0c10] shadow-2xl flex flex-col overflow-hidden",
        className
      )}
    >
      {/* Terminal Top Control Bar */}
      <div className="flex items-center justify-between px-4 py-2.5 border-b border-border/60 bg-[#121216] select-none">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5">
            <div className="h-3 w-3 rounded-full bg-red-500/80" />
            <div className="h-3 w-3 rounded-full bg-amber-500/80" />
            <div className="h-3 w-3 rounded-full bg-emerald-500/80" />
          </div>
          <div className="h-4 w-[1px] bg-white/10" />
          <div className="flex items-center gap-2">
            <Terminal className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-mono text-zinc-300 font-medium">{title}</span>
          </div>
          {isConnected && !isCompleted && (
            <span className="inline-flex items-center gap-1 text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Radio className="h-2.5 w-2.5 animate-pulse" />
              LIVE
            </span>
          )}
          {isCompleted && (
            <span className="inline-flex items-center text-[10px] font-mono px-2 py-0.5 rounded-full bg-zinc-800 text-zinc-400 border border-zinc-700">
              COMPLETED
            </span>
          )}
        </div>

        {/* Action icons */}
        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="sm"
            onClick={handleCopyLogs}
            className="h-7 px-2 text-xs text-zinc-400 hover:text-white"
            title="Copy logs"
          >
            {copied ? <Check className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
          </Button>

          <Button
            variant="ghost"
            size="sm"
            onClick={handleDownloadLogs}
            className="h-7 px-2 text-xs text-zinc-400 hover:text-white"
            title="Download log file"
          >
            <Download className="h-3.5 w-3.5" />
          </Button>

          {onClear && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onClear}
              className="h-7 px-2 text-xs text-zinc-400 hover:text-red-400"
              title="Clear terminal"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          )}
        </div>
      </div>

      {/* Terminal Log Output Stream */}
      <div
        ref={containerRef}
        onScroll={handleScroll}
        className="flex-1 p-4 overflow-y-auto max-h-[460px] min-h-[260px] space-y-0.5 font-mono text-xs bg-[#09090d]"
      >
        {logs.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-48 text-zinc-500 space-y-2">
            <Terminal className="h-8 w-8 text-zinc-700 animate-pulse" />
            <p className="text-xs">Waiting for build worker output stream...</p>
          </div>
        ) : (
          logs.map((line, idx) => renderLogLine(line, idx))
        )}
      </div>

      {/* Auto-scroll toggle bottom banner */}
      {!autoScroll && (
        <div className="p-2 border-t border-border/40 bg-zinc-900/90 flex justify-center">
          <Button
            size="sm"
            variant="outline"
            onClick={() => {
              setAutoScroll(true);
              if (containerRef.current) {
                containerRef.current.scrollTop = containerRef.current.scrollHeight;
              }
            }}
            className="h-7 text-xs gap-1.5 bg-black/60 border-white/20 text-white"
          >
            <ArrowDown className="h-3 w-3" />
            <span>Scroll to bottom</span>
          </Button>
        </div>
      )}
    </div>
  );
}

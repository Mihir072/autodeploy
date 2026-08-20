import React, { useState } from "react";
import { EnvironmentVariable } from "@/types";
import { Plus, Trash2, Eye, EyeOff, KeyRound, Lock } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface EnvVarEditorProps {
  variables: EnvironmentVariable[];
  onChange: (variables: EnvironmentVariable[]) => void;
  readOnly?: boolean;
}

export function EnvVarEditor({ variables, onChange, readOnly = false }: EnvVarEditorProps) {
  const [showValues, setShowValues] = useState<Record<number, boolean>>({});

  const handleToggleValue = (index: number) => {
    setShowValues((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  const handleAddRow = () => {
    onChange([...variables, { key: "", value: "", isSecret: false }]);
  };

  const handleRemoveRow = (index: number) => {
    const updated = variables.filter((_, i) => i !== index);
    onChange(updated);
  };

  const handleChangeKey = (index: number, key: string) => {
    const updated = [...variables];
    updated[index].key = key;
    onChange(updated);
  };

  const handleChangeValue = (index: number, value: string) => {
    const updated = [...variables];
    updated[index].value = value;
    onChange(updated);
  };

  const handleToggleSecret = (index: number) => {
    const updated = [...variables];
    updated[index].isSecret = !updated[index].isSecret;
    onChange(updated);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <KeyRound className="h-4 w-4 text-muted-foreground" />
          <span className="text-xs font-mono uppercase tracking-wider text-muted-foreground">
            Environment Variables
          </span>
        </div>
        {!readOnly && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={handleAddRow}
            className="h-7 text-xs gap-1.5"
          >
            <Plus className="h-3.5 w-3.5" />
            <span>Add Variable</span>
          </Button>
        )}
      </div>

      {variables.length === 0 ? (
        <div className="p-6 rounded-lg border border-dashed border-border/80 bg-card/20 text-center text-xs text-muted-foreground space-y-2">
          <p>No environment variables configured yet.</p>
          {!readOnly && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleAddRow}
              className="h-7 text-xs"
            >
              Add first variable
            </Button>
          )}
        </div>
      ) : (
        <div className="space-y-2">
          {variables.map((env, index) => (
            <div
              key={index}
              className="flex items-center gap-2 p-2 rounded-lg border border-border/60 bg-card/60"
            >
              <Input
                type="text"
                placeholder="VARIABLE_NAME"
                value={env.key}
                disabled={readOnly}
                onChange={(e) => handleChangeKey(index, e.target.value)}
                className="h-8 font-mono text-xs uppercase flex-1 bg-background/50"
              />

              <div className="relative flex-1">
                <Input
                  type={showValues[index] ? "text" : "password"}
                  placeholder="value"
                  value={env.value}
                  disabled={readOnly}
                  onChange={(e) => handleChangeValue(index, e.target.value)}
                  className="h-8 font-mono text-xs pr-8 bg-background/50"
                />
                <button
                  type="button"
                  onClick={() => handleToggleValue(index)}
                  className="absolute right-2 top-2 text-muted-foreground hover:text-white transition-colors"
                  title={showValues[index] ? "Hide value" : "Show value"}
                >
                  {showValues[index] ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                </button>
              </div>

              {!readOnly && (
                <>
                  <button
                    type="button"
                    onClick={() => handleToggleSecret(index)}
                    className={`p-1.5 rounded text-xs transition-colors ${
                      env.isSecret ? "text-amber-400 bg-amber-500/10" : "text-zinc-500 hover:text-zinc-300"
                    }`}
                    title={env.isSecret ? "Marked as sensitive secret (encrypted)" : "Plain environment variable"}
                  >
                    <Lock className="h-3.5 w-3.5" />
                  </button>

                  <button
                    type="button"
                    onClick={() => handleRemoveRow(index)}
                    className="p-1.5 text-zinc-500 hover:text-red-400 rounded transition-colors"
                    title="Remove variable"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

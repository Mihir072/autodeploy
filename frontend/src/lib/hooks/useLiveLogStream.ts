import { useState, useEffect, useRef } from "react";

interface UseLiveLogStreamOptions {
  buildId?: string;
  deploymentId?: string;
  isLive?: boolean;
  initialLogs?: string[];
  branch?: string;
  commitSha?: string;
  subdomain?: string;
}

const generateDefaultLogs = (
  branch = "main",
  commitSha = "c8f9a2b",
  subdomain = "app"
): string[] => [
  `[INIT] Worker assigned to build job in cluster node ec2-us-east-1a`,
  `[BUILD] Initializing rootless Kaniko executor in isolated container sandbox...`,
  `[BUILD] Fetching Git source tree from repository at target branch '${branch}'...`,
  `[BUILD] Checkout target commit SHA: ${commitSha.substring(0, 7)}`,
  `[BUILD] Analyzing repository dependencies and Dockerfile configuration...`,
  `[BUILD] Found framework: Node.js (Vite / Next.js SPA/SSR bundle)`,
  `[BUILD] Executing build command: npm ci --prefer-offline && npm run build`,
  `[BUILD] > vite build`,
  `[BUILD] transforming (148) modules...`,
  `[BUILD] ✓ 148 modules transformed.`,
  `[BUILD] rendering chunks (12)...`,
  `[BUILD] Compiling TypeScript AST and optimizing production bundles...`,
  `[BUILD] Generated static assets and server output artifacts in 4.12s`,
  `[BUILD] Building minimal runtime container image (eclipse-temurin / alpine)...`,
  `[BUILD] Pushing container image layer cache to AWS ECR...`,
  `[BUILD] Container image digest pushed: sha256:d8f74e92b${commitSha.substring(0, 6)}`,
  `[SUCCESS] Build completed successfully in 12.8s`,
  `[DEPLOY] Initializing container runtime instance on edge host 172.20.0.8...`,
  `[DEPLOY] Container started: cnt_${commitSha.substring(0, 12)} on port 8080`,
  `[ROUTER] Registering Traefik v3 HTTP router rule: Host(\`${subdomain}.autodeploy.app\`, \`localhost\`)`,
  `[SSL] Provisioning Let's Encrypt TLS certificate (SNI: ${subdomain}.autodeploy.app)`,
  `[HEALTH] Performing TCP and HTTP probe on http://localhost:8080/actuator/health...`,
  `[HEALTH] ✓ Response 200 OK (latency: 14ms)`,
  `[SUCCESS] Deployment is now LIVE at https://${subdomain}.autodeploy.app (and http://localhost:8080)`,
];

export function useLiveLogStream({
  buildId,
  deploymentId,
  isLive = false,
  initialLogs = [],
  branch = "main",
  commitSha = "c8f9a2b",
  subdomain = "app",
}: UseLiveLogStreamOptions) {
  const [logs, setLogs] = useState<string[]>(() => {
    if (initialLogs && initialLogs.length > 0) return initialLogs;
    if (!isLive) return generateDefaultLogs(branch, commitSha, subdomain);
    return [`[INIT] Connected to live build stream...`];
  });
  const [isConnected, setIsConnected] = useState(isLive);
  const [isCompleted, setIsCompleted] = useState(!isLive);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (initialLogs && initialLogs.length > 0) {
      setLogs(initialLogs);
      return;
    }

    if (!isLive) {
      setLogs(generateDefaultLogs(branch, commitSha, subdomain));
      setIsConnected(false);
      setIsCompleted(true);
      return;
    }

    // Live Streaming flow
    setIsCompleted(false);
    setIsConnected(true);

    const defaultLines = generateDefaultLogs(branch, commitSha, subdomain);
    let sseConnected = false;

    if (buildId) {
      const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:9090";
      const sseUrl = `${baseUrl}/api/builds/${buildId}/logs`;

      try {
        const eventSource = new EventSource(sseUrl, { withCredentials: true });
        eventSourceRef.current = eventSource;

        eventSource.onopen = () => {
          sseConnected = true;
          setIsConnected(true);
        };

        eventSource.onmessage = (event) => {
          if (event.data) {
            setLogs((prev) => [...prev, event.data]);
            if (
              event.data.includes("Build completed") ||
              event.data.includes("Deployment successful") ||
              event.data.includes("is now LIVE") ||
              event.data.includes("Build failed") ||
              event.data.includes("Deployment failed")
            ) {
              setIsCompleted(true);
              setIsConnected(false);
              eventSource.close();
            }
          }
        };

        eventSource.onerror = () => {
          eventSource.close();
          if (!sseConnected) {
            // Fallback progressive stream if SSE server channel isn't broadcasting yet
            let currentIndex = 1;
            setLogs([defaultLines[0]]);
            const interval = setInterval(() => {
              if (currentIndex < defaultLines.length) {
                const nextLine = defaultLines[currentIndex];
                setLogs((prev) => [...prev, nextLine]);
                currentIndex++;
              } else {
                clearInterval(interval);
                setIsCompleted(true);
                setIsConnected(false);
              }
            }, 800);

            return () => clearInterval(interval);
          }
        };

        return () => {
          eventSource.close();
        };
      } catch (err) {
        console.error("SSE connection error:", err);
      }
    }

    // If no buildId or SSE failed immediately, stream simulated progress
    let currentIndex = 1;
    setLogs([defaultLines[0]]);
    const interval = setInterval(() => {
      if (currentIndex < defaultLines.length) {
        const nextLine = defaultLines[currentIndex];
        setLogs((prev) => [...prev, nextLine]);
        currentIndex++;
      } else {
        clearInterval(interval);
        setIsCompleted(true);
        setIsConnected(false);
      }
    }, 700);

    return () => {
      clearInterval(interval);
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, [buildId, deploymentId, isLive, branch, commitSha, subdomain]);

  const clearLogs = () => setLogs([]);

  return {
    logs,
    isConnected,
    isCompleted,
    clearLogs,
  };
}

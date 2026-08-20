export type DeploymentStatus = "QUEUED" | "BUILDING" | "DEPLOYING" | "LIVE" | "FAILED" | "CANCELLED" | "IDLE";
export type BuildStatus = "QUEUED" | "IN_PROGRESS" | "COMPLETED" | "FAILED" | "CANCELLED";
export type DomainStatus = "PENDING_VERIFICATION" | "VERIFIED" | "FAILED";
export type SslStatus = "PENDING" | "ACTIVE" | "FAILED";

export interface User {
  id: string;
  githubId: string;
  username: string;
  avatarUrl: string;
  email?: string;
  createdAt: string;
}

export interface AuthSession {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface EnvironmentVariable {
  id?: string;
  key: string;
  value: string;
  isSecret?: boolean;
}

export interface Project {
  id: string;
  name: string;
  slug: string;
  repositoryUrl: string;
  repositoryName: string;
  branch: string;
  buildCommand?: string;
  outputDirectory?: string;
  dockerfilePath?: string;
  environmentVariables?: EnvironmentVariable[];
  latestDeployment?: DeploymentSummary;
  subdomain: string;
  customDomain?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeploymentSummary {
  id: string;
  status: DeploymentStatus;
  commitSha: string;
  commitMessage?: string;
  branch: string;
  url?: string;
  triggeredBy?: string;
  durationSeconds?: number;
  createdAt: string;
}

export interface Deployment extends DeploymentSummary {
  projectId: string;
  buildId?: string;
  logs?: string[];
  errorMessage?: string;
  traefikUrl?: string;
}

export interface Build {
  id: string;
  projectId: string;
  deploymentId?: string;
  status: BuildStatus;
  commitSha: string;
  imageTag?: string;
  logStreamChannel?: string;
  startedAt?: string;
  finishedAt?: string;
  durationSeconds?: number;
}

export interface DomainRecord {
  id: string;
  projectId: string;
  domainName: string;
  status: DomainStatus;
  sslStatus: SslStatus;
  verificationToken?: string;
  cnameTarget?: string;
  createdAt: string;
}

export interface GitHubRepository {
  id: number;
  name: string;
  fullName: string;
  owner: string;
  ownerAvatarUrl: string;
  private: boolean;
  defaultBranch: string;
  updatedAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

/////////////////////// Results ////////////////////////////
export interface LinterResult {
    type: string
};

export const SUCCESSFUL_RESULT = 'SuccessfulResult';
export class SuccesfulResult {
    readonly type = SUCCESSFUL_RESULT;
    readonly spoon: SpoonResult;
    readonly pmd: PMDResult;
    readonly spotbugs: SpotBugsResult;
    readonly cpd: CPDResult;
    readonly compilation: CompilationResult;
};

export class PMDResult {
    readonly problems: Problem[];
};

export class SpoonResult {
    readonly problems: Problem[];
};

export class SpotBugsResult {
    readonly problems: Problem[];
}

export class CPDResult {
    readonly problems: Problem[];
}

export class CompilationResult {
    readonly diagnostics: CompilationDiagnostic[];
}

export const COMPILATION_ERROR_RESULT = 'CompilationErrorResult';
export class CompilationErrorResult {
    readonly type = COMPILATION_ERROR_RESULT;
    readonly description: string;
};

export const FILE_CLIENT_ERROR_RESULT = 'FileClientErrorResult';
export class FileClientErrorResult {
    readonly type = 'FileClientErrorResult';
    readonly description: string;
};

export const INTERNAL_ERROR_RESULT = 'FileClientErrorResult';
export class InternalErrorResult {
    readonly type = 'FileClientErrorResult';
    readonly description: string;
};

export const NETWORK_ERROR_RESULT = 'NetworkErrorResult';
export class NetworkErrorResult {
    readonly type = NETWORK_ERROR_RESULT;
};

////////////////////// Problems /////////////////////////

export interface Problem {
    readonly type: string;
    readonly description: string;
    readonly category: string;
    readonly explanation: string;
    readonly priority: string;
};

export const IN_CODE_PROBLEM = 'InCodeTransferProblem';
export class InCodeProblem {
    readonly type = IN_CODE_PROBLEM;
    readonly description: string;
    readonly column: number;
    readonly line: number;
    readonly category: string;
    readonly displayPath: string;
    readonly filePath: string;
    readonly explanation: string;
    readonly priority: string;
};

export namespace ProblemPriority {
    export const SEVERE = 'SEVERE';
    export const POSSIBLE_SEVERE = 'POSSIBLE_SEVERE';
    export const FIX_RECOMMENDED = 'FIX_RECOMMENDED';
    export const INFO = 'INFO';
}

//////////////////// Compilation ///////////////////////

export class CompilationDiagnostic {
    readonly message: string;
    readonly line: number;
    readonly column: number;
    readonly className: string;
}
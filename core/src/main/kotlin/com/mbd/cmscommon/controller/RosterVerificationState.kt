package com.mbd.cmscommon.controller

enum class RosterVerificationState {
    CHECKING,
    MATCHED,
    RELINK,
    IDENTITY_MISMATCH,
    MISSING,
    FAILED,
}

package com.robotutor.iot.authService.gateway

import com.robotutor.iot.authService.gateway.views.ValidateAccountResponse
import com.robotutor.iot.utils.models.UserAuthenticationData
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AccountServiceGateway(
//    private val accountController: AccountController,
//    private val boardController: BoardController
) {
    fun isValidAccountAndRole(userId: String, accountId: String, roleId: String): Mono<ValidateAccountResponse> {
//        return accountController.isValidAccount(
//            accountValidationRequest = AccountValidationRequest(accountId = accountId, roleId = roleId),
//            userAuthenticationData = UserAuthenticationData(
//                userId = userId,
//                accountId = accountId,
//                roleId = roleId,
//                boardId = null
//            )
//        )
//            .map {
//                ValidateAccountResponse(true)
//            }
        return Mono.empty()
    }

    fun isValidBoard(boardId: String, authenticationData: UserAuthenticationData): Mono<Boolean> {
//        return boardController.isValidBoardId(userAuthenticationData = authenticationData, boardId = boardId)
        return Mono.empty()
    }
}

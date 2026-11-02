/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.web.posting;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fireflyframework.web.idempotency.annotation.DisableIdempotency;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.buffer.*;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
@RestController @ConditionalOnProperty(name="firefly.terminal.managed-posting.enabled",havingValue="true")
@RequestMapping("/api/v1/staff/tenants/{tenantId}/books/{bookId}/ledger-journals")
@SecurityRequirement(name="staffBearer") @Tag(name="StaffLedgerPostings",description="Staff submits and recovers an already committed internal USD transfer through Accounting Domain")
@ApiResponses({
 @ApiResponse(responseCode="200",content=@Content(mediaType="application/json",schema=@Schema(implementation=StaffPostingResponse.class))),
 @ApiResponse(responseCode="400",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="401",description="Current authentication failure; security filters can respond before application error projection",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="403",description="Current authorization failure; prior effects remain uncharacterized",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="404",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="409",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="422",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="503",content=@Content(mediaType="application/json",schema=@Schema(oneOf={StaffPostingResponse.class,StaffPostingPreDispatchError.class}))),
 @ApiResponse(responseCode="4XX",description="Other exact Domain invocation refusal",content=@Content(mediaType="application/json",schema=@Schema(implementation=StaffPostingResponse.class))),
 @ApiResponse(responseCode="default",description="Unavailable response; retain complete original action and recover through the same route")})
public class StaffLedgerPostingsController {
    private final StaffPostingService postings;
    public StaffLedgerPostingsController(StaffPostingService postings){this.postings=postings;}
    @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE) @DisableIdempotency
    @Operation(operationId="postStaffLedgerJournal",summary="Submit the exact approved original transfer journal through Domain",description="Experience never calls Core. Core independently verifies the genuine current Ledger COMMIT and four-eyes mapping approval. An uncertain response does not authorize a replacement action or compensation.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required=true,content=@Content(mediaType="application/json",schema=@Schema(implementation=StaffPostingRequest.class)))
    public Mono<ResponseEntity<StaffPostingResponse>> post(@PathVariable String tenantId,@PathVariable String bookId,ServerHttpRequest request){return invoke(tenantId,bookId,request,false);}
    @PostMapping(value="/recovery",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE) @DisableIdempotency
    @Operation(operationId="recoverStaffLedgerJournal",summary="Recover the same complete original action through Domain",description="Recovery does not issue another monetary POST. Missing evidence or current refusal is not an audited no-effect closure.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required=true,content=@Content(mediaType="application/json",schema=@Schema(implementation=StaffPostingRequest.class)))
    public Mono<ResponseEntity<StaffPostingResponse>> recover(@PathVariable String tenantId,@PathVariable String bookId,ServerHttpRequest request){return invoke(tenantId,bookId,request,true);}
    private Mono<ResponseEntity<StaffPostingResponse>> invoke(String tenant,String book,ServerHttpRequest request,boolean recovery){
        var headers=request.getHeaders().get(HttpHeaders.AUTHORIZATION);if(headers==null||headers.size()!=1||!headers.getFirst().startsWith("Bearer "))return Mono.error(new StaffPostingPreDispatchException(HttpStatus.UNAUTHORIZED));
        final StaffPostingCallerToken token;try{token=new StaffPostingCallerToken(headers.getFirst().substring(7));}catch(IllegalArgumentException invalid){return Mono.error(new StaffPostingPreDispatchException(HttpStatus.UNAUTHORIZED));}
        return DataBufferUtils.join(request.getBody(),1048576).switchIfEmpty(Mono.error(new StaffPostingPreDispatchException(HttpStatus.BAD_REQUEST))).onErrorMap(error->new StaffPostingPreDispatchException(HttpStatus.BAD_REQUEST))
            .map(buffer->{try{var body=new byte[buffer.readableByteCount()];buffer.read(body);return body;}finally{DataBufferUtils.release(buffer);}})
            .flatMap(body->postings.submit(tenant,book,body,recovery)).map(value->{
                int status=switch(value.status()){case COMMITTED->200;case NOT_OBSERVED->404;case CURRENT_INVOCATION_REFUSED,DOMAIN_PRE_DISPATCH_REFUSED->value.domainStatus();default->503;};
                return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).contentType(MediaType.APPLICATION_JSON).body(value);
            }).contextWrite(context->context.put(StaffPostingCallerToken.class,token));
    }
}

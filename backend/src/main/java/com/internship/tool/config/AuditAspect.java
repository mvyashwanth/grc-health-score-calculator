package com.internship.tool.config;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(
        pointcut = "execution(* com.internship.tool.service.HealthRecordService.create(..))",
        returning = "result"
    )
    public void afterCreate(JoinPoint jp, Object result) {
        saveLog("HealthRecord", getIdFromResult(result), "CREATE");
    }

    @AfterReturning(
        pointcut = "execution(* com.internship.tool.service.HealthRecordService.update(..))",
        returning = "result"
    )
    public void afterUpdate(JoinPoint jp, Object result) {
        saveLog("HealthRecord", getIdFromResult(result), "UPDATE");
    }

    @AfterReturning(
        pointcut = "execution(* com.internship.tool.service.HealthRecordService.softDelete(..))"
    )
    public void afterDelete(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long id = args.length > 0 ? (Long) args[0] : null;
        saveLog("HealthRecord", id, "DELETE");
    }

    private void saveLog(String entity, Long entityId, String action) {
        try {
            String user = "anonymous";
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) user = auth.getName();

            auditLogRepository.save(AuditLog.builder()
                    .entityName(entity)
                    .entityId(entityId)
                    .action(action)
                    .performedBy(user)
                    .build());
        } catch (Exception e) {
            log.warn("Audit log failed: {}", e.getMessage());
        }
    }

    private Long getIdFromResult(Object result) {
        try {
            return (Long) result.getClass().getMethod("getId").invoke(result);
        } catch (Exception e) {
            return null;
        }
    }
}

package er;

import java.time.Instant;

class AlarmEntity {
    private final int AlarmEntryId;
    private final int ObjectId;
    private final String TechnicalDescription;
    private final String ObjectDescription;
    private final int PriorityNumber;
    private final Instant alarmRegistrationTime;

    AlarmEntity(int AlarmEntryId,
                int ObjectId,
                String TechnicalDescription,
                String ObjectDescription,
                int PriorityNumber,
                Instant alarmRegistrationTime
    ) {
        this.AlarmEntryId = AlarmEntryId;
        this.ObjectId = ObjectId;
        this.TechnicalDescription = TechnicalDescription;
        this.ObjectDescription = ObjectDescription;
        this.PriorityNumber = PriorityNumber;
        this.alarmRegistrationTime = alarmRegistrationTime;
    }

    int getAlarmEntryId() {
        return AlarmEntryId;
    }

    int getObjectId() {
        return ObjectId;
    }

    int getPriorityNumber() {
        return PriorityNumber;
    }

    String getObjectDescription() {
        return ObjectDescription;
    }

    String getTechnicalDescription() {
        return TechnicalDescription;
    }

    Instant getAlarmRegistrationTime() {
        return alarmRegistrationTime;
    }
}
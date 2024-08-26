


@Name('OccurredEventsActivation')
select *
from pattern [
    every (
        activity_2 = ubt.process_analytics.esper.EPPMEventType(activity = 'Electronic invoice received')
 or         activity_1 = ubt.process_analytics.esper.EPPMEventType(activity = 'Paper invoice received')
    )
    ].win:time(50 sec)


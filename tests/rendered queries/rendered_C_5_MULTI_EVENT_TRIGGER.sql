


@Name('MultiEventTrigger')
select *
from pattern [
    every (
        activity_2 = ubt.process_analytics.esper.EPPMEventType(activity = '${eventMap.get($eventKey)}')
 ->         activity_1 = ubt.process_analytics.esper.EPPMEventType(activity = '${eventMap.get($eventKey)}')
        )
    ].win:time(50 sec)



@Name('EventBasedUnsubscription')
select *
from pattern [
    every (
    e1 = ubt.process_analytics.esper.EPPMEventType(activity = 'Electronic invoice received')
    -> e2 = ubt.process_analytics.esper.EPPMEventType(activity = 'Electronic invoice received')
    -> not ubt.process_analytics.esper.EPPMEventType(activity = 'Electronic invoice received')
    -> not ubt.process_analytics.esper.EPPMEventType(activity = 'Paper invoice received')
    )
    ]



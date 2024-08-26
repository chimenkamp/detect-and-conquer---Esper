@Name('ReachableSubscriptions')
select *
from pattern [
    every e = ubt.process_analytics.esper.EPPMEventType(activity = 'Electronic invoice received')
    ].win:time(50 sec);

insert into SubscriptionStream
select *
from ubt.process_analytics.esper.EPPMEventType as e
where e.activity in (
                   'Electronic invoice received'     , 'Electronic invoice received'     , 'Paper invoice received' 
    )


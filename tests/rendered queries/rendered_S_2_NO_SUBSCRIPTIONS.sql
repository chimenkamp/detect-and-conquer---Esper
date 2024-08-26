@Name('NoSubscriptions')
select *
from pattern [
    every e = ubt.process_analytics.esper.EPPMEventType(activity = 'Electronic invoice received')
    ].win:time(50 sec)



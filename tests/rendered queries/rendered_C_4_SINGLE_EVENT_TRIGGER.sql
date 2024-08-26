@Name('SingleEventTrigger')
select *
from ubt.process_analytics.esper.EPPMEventType.win:time(50 sec) as event
where event.activity = 'Electronic invoice received'



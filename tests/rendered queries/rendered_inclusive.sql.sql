

SELECT * FROM PATTERN [
EVERY
    (activity_3 = ubt.process_analytics.esper.EPPMEventType(activity ='Production Planning'))
        OR
    (activity_2 = ubt.process_analytics.esper.EPPMEventType(activity ='Quality Control'))
        OR
    (activity_1 = ubt.process_analytics.esper.EPPMEventType(activity ='Manufacturing Process'))
        OR
    (activity_4 = ubt.process_analytics.esper.EPPMEventType(activity ='Prepare Workstations'))
].win:time(50 sec)


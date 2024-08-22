SELECT e1.activity AS firstActivity, e2.activity AS secondActivity, e1.timestamp AS firstTimestamp, e2.timestamp AS secondTimestamp
FROM pattern [
    every e1 = ubt.process_analytics.esper.EPPMEventType ->
    e2 = ubt.process_analytics.esper.EPPMEventType(
        e2.activity != e1.activity AND
        e2.timestamp BETWEEN e1.timestamp AND e1.timestamp + 10000
    )
]
WHERE e1.timestamp BETWEEN e2.timestamp - 10000 AND e2.timestamp + 10000;


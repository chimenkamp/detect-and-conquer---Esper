

SELECT * FROM PATTERN [
    EVERY
    (


        (
            activity_2 = ubt.process_analytics.esper.EPPMEventType(activity ='Electronic invoice received')
            -> NOT ubt.process_analytics.esper.EPPMEventType(
                timestamp > activity_2.timestamp AND
                activity IN ('Electronic invoice received', 'Paper invoice received') AND
                caseID = activity_2.caseID
            )
        )

    OR

            (
            activity_2 = ubt.process_analytics.esper.EPPMEventType(activity ='Electronic invoice received')
            -> NOT ubt.process_analytics.esper.EPPMEventType(
                timestamp > activity_2.timestamp AND
                activity IN ('Electronic invoice received', 'Paper invoice received') AND
                caseID = activity_2.caseID
                )
            )
        OR

        (
            activity_1 = ubt.process_analytics.esper.EPPMEventType(activity ='Paper invoice received')
            -> NOT ubt.process_analytics.esper.EPPMEventType(
                timestamp > activity_1.timestamp AND
                activity IN ('Electronic invoice received', 'Paper invoice received') AND
                caseID = activity_1.caseID
            )
        )

    )
    ].win:time(50 sec)


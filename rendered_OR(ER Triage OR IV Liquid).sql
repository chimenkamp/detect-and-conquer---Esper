

SELECT * FROM PATTERN [
    EVERY
    (


        (
            activity_2 = ubt.process_analytics.esper.EPPMEventType(activity ='IV Liquid')
            -> NOT ubt.process_analytics.esper.EPPMEventType(
                timestamp > activity_2.timestamp AND
                activity IN ('IV Liquid', 'ER Triage') AND
                caseID = activity_2.caseID
            )
        )

    OR

            (
            activity_2 = ubt.process_analytics.esper.EPPMEventType(activity ='IV Liquid')
            -> NOT ubt.process_analytics.esper.EPPMEventType(
                timestamp > activity_2.timestamp AND
                activity IN ('IV Liquid', 'ER Triage') AND
                caseID = activity_2.caseID
                )
            )
        OR

        (
            activity_1 = ubt.process_analytics.esper.EPPMEventType(activity ='ER Triage')
            -> NOT ubt.process_analytics.esper.EPPMEventType(
                timestamp > activity_1.timestamp AND
                activity IN ('IV Liquid', 'ER Triage') AND
                caseID = activity_1.caseID
            )
        )

    )
    ].win:time(50 sec)


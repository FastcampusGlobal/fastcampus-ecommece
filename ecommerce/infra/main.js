const { Buffer } = require('buffer');

exports.handler = async (event) => {
    console.log(JSON.stringify(event))
    const transformedRecords = event.records.map((record) => {
        // Decode the base64 data
        const payload = Buffer.from(record.data, 'base64').toString('utf-8');
        const dynamodbRecord = JSON.parse(payload);

        // Extract the NewImage from the record
        const newImage = dynamodbRecord?.dynamodb?.NewImage || {};

        // Transform the DynamoDB JSON structure to a flat structure
        const transformedData = {
            trip_id: parseInt(newImage?.trip_id?.N || "0"),
            driver_id: parseInt(newImage?.driver_id?.N || "0"),
            driver_name: newImage?.driver_name?.S || "",
            driver_phone: newImage?.driver_phone?.S || "",
            vehicle_id: newImage?.vehicle_id?.S || "",
            vehicle_model: newImage?.vehicle_model?.S || "",
            passenger_id: parseInt(newImage?.passenger_id?.N || "0"),
            passenger_name: newImage?.passenger_name?.S || "",
            passenger_phone: newImage?.passenger_phone?.S || "",
            start_time: newImage?.start_time?.S || "",
            end_time: newImage?.end_time?.S || "",
            start_latitude: parseFloat(newImage?.start_latitude?.N || "0"),
            start_longitude: parseFloat(newImage?.start_longitude?.N || "0"),
            end_latitude: parseFloat(newImage?.end_latitude?.N || "0"),
            end_longitude: parseFloat(newImage?.end_longitude?.N || "0"),
            trip_status: newImage?.trip_status?.S || "",
            recorded_time: newImage?.recorded_time?.S || "",
            current_latitude: parseFloat(newImage?.current_latitude?.N || "0"),
            current_longitude: parseFloat(newImage?.current_longitude?.N || "0"),
            accuracy: parseFloat(newImage?.accuracy?.N || "0"),
            speed: parseFloat(newImage?.speed?.N || "0")
        };

        // Encode the transformed data back to Base64
        const transformedDataString = JSON.stringify(transformedData);
        const transformedDataBase64 = Buffer.from(transformedDataString).toString('base64');

        return {
            recordId: record.recordId,
            result: "Ok",
            data: transformedDataBase64
        };
    });

    return { records: transformedRecords };
};
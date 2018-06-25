def call(Map parameters = [:]) {

    return {
        def headers = parameters.get('headers', {})

        return [
                headers: headers()
        ]

    }
}
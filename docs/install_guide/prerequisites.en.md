# Prerequisites

## Hardware

The absolute minimum system requirement to test the core services (gateway, GeoServer, GeoNetwork, console) is 2 cores and 8Gb RAM.

We recommend at least 4 cores and 32Gb RAM for a tiny production instance which includes MapStore, datahub & the datafeeder.

Depending on the installation type, hardware requirements will slightly differ. Eg. containerized systems tend to use more RAM, as each container comes with its own JVM, see for instance the [resource allocations and limits to run geOrchestra with Kubernetes](https://github.com/georchestra/helm-charts/tree/main/georchestra#resources-allocations-and-limits).


As of 2026, typical "real-life" production setups range from 8 to 128 cores, and from 64 to 256 Gb RAM.

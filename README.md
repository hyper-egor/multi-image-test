# multi-image-test
multimodule project to explore e2e tests of several dockered modules on spring boot

Module-A - a separate dockered module wich receives task requests via HTTP and transmits them to Module-B

Module-B - receives requests from Module-A via Solace, makes some operation on it sends the reply to some outside endpoint, listening on another Solace topic
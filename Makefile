###############################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
###############################################################################


SHELL := /bin/bash
.SHELLFLAGS := -Eeuoc pipefail
.ONESHELL:

include Sharechat-Utils.mk

DockerImageName := flink-kubernetes-operator
ArmoryRepos := mum-armory.platform.internal/ai-infra sgp-armory.platform.internal/ai-infra

build-docker-latest:
	docker build . --tag $(DockerImageName):latest

generate-docker-tag:
	# We compute docker tag as following: we take git tag if it matches the pattern release-sharechat-*
    # if suh tag exists, we remove release- prefix and use the rest as the docker tag. If such tag
    # doesn't exist, we take last git commit hash instead.
	rm -rf tmp-docker-tag.log
	(git describe --tag --exact-match --match "release-sharechat-*" 2> /dev/null || git rev-parse HEAD) \
		| sed 's/^release-//' >> tmp-docker-tag.log

push-docker: build-docker-latest require-var.ARMORY_USERNAME require-var.ARMORY_PASSWORD
	@ for repo in $(ArmoryRepos); do \
	  echo -n "$${ARMORY_PASSWORD}" | docker login --username "$${ARMORY_USERNAME}" --password-stdin $$repo ;\
	  dockerImage=$$repo/$(DockerImageName):$${dockerTag}
	  docker tag $(DockerImageName):latest $${dockerImage} ;\
	  docker push $${dockerImage} ;\
	  echo "Pushed Flink Kubernetes Operator image $$dockerImage"
	  docker logout $$repo ;\
	done

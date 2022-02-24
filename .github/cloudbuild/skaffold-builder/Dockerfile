# Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# https://github.com/GoogleCloudPlatform/cloud-builders/tree/master/kubectl
FROM gcr.io/cloud-builders/kubectl

RUN mkdir -p /builder/bin && \
  apt-get update && \
  apt-get install -y curl && \
  curl -sSLo /builder/bin/skaffold https://storage.googleapis.com/skaffold/releases/latest/skaffold-linux-amd64 && \
  chmod +x /builder/bin/skaffold && \
  apt-get remove --purge -y curl && \
  apt-get --purge -y autoremove && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

ENV PATH=/builder/bin/:$PATH
ENV DOCKER_CONFIG=/builder/home/.docker

COPY skaffold.bash /builder/skaffold.bash
RUN chmod +700 /builder/skaffold.bash
ENTRYPOINT ["/builder/skaffold.bash"]

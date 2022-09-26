{{- define "deployment.envs" }}
- name: JAVA_OPTS
  value: '{{ tpl .Values.app.javaOpts . }}'
- name: SERVER_PORT
  value: '{{ include "hocs-app.port" . }}'
{{- end -}}

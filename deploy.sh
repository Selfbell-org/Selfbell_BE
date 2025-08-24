#!/usr/bin/env bash
set -euo pipefail

### ====== CONFIG ======
APP_NAME="${APP_NAME:-selfbell}"
PORT="${PORT:-8080}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
LOG_DIR="${LOG_DIR:-logs}"
APP_LOG="${APP_LOG:-${LOG_DIR}/app.log}"
PID_FILE="${PID_FILE:-${LOG_DIR}/app.pid}"

### ====== UTILS ======
c_green(){ echo -e "\033[32m$*\033[0m"; }
c_yellow(){ echo -e "\033[33m$*\033[0m"; }
c_red(){ echo -e "\033[31m$*\033[0m"; }

pid_on_port() {
  lsof -ti tcp:"$PORT" || true
}

kill_pid() {
  local pid="$1"
  if [[ -n "${pid}" ]]; then
    c_yellow "PID ${pid} 종료 시도..."
    kill "${pid}" || true
    sleep 1
    if ps -p "${pid}" >/dev/null 2>&1; then
      c_yellow "SIGKILL 진행 (PID ${pid})"
      kill -9 "${pid}" || true
    fi
  fi
}

### ====== 1) Gradle 빌드 ======
c_green "1) Gradle 빌드 시작"
./gradlew clean build -x test \
  -Dspring.profiles.active="${SPRING_PROFILE}" \
  -Duser.language=ko -Duser.country=KR

### ====== 2) 기존 포트 프로세스 종료 ======
c_green "2) 기존 ${PORT} 포트 점유 프로세스 종료"
PID="$(pid_on_port)"
if [[ -n "${PID}" ]]; then
  c_yellow "포트 ${PORT} 사용 중 PID: ${PID}"
  kill_pid "${PID}"
else
  c_green "포트 ${PORT}는 사용 중이 아닙니다."
fi

### ====== 3) JAR 실행 준비 ======
c_green "3) JAR 실행 준비"
cd build/libs
JAR_PATH=""
for jar in *.jar; do
  if [[ -f "$jar" && "$jar" != *"plain"* ]]; then
    JAR_PATH="$jar"
    break
  fi
done
if [[ -z "${JAR_PATH}" ]]; then
  c_red "실행 가능한 JAR 파일을 찾을 수 없습니다."
  exit 1
fi
c_green "실행 대상 JAR: ${JAR_PATH}"
mkdir -p "../${LOG_DIR}"
rm -f "../${APP_LOG}" "../${PID_FILE}"

### ====== 4) 백그라운드 실행 ======
c_green "4) 애플리케이션 백그라운드 실행"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
nohup java -jar "${JAR_PATH}" > "../${APP_LOG}" 2>&1 &

NEW_PID=$!
echo "${NEW_PID}" > "../${PID_FILE}"
c_green "시작 PID: ${NEW_PID}"
sleep 3

### ====== 5) 로그 확인 ======
cd ..
c_green "5) 로그 tail (마지막 100줄)"
tail -f "${APP_LOG}"

### ====== 6) 포트 리스닝 확인 ======
c_green "6) 포트 ${PORT} 리스닝 확인"
if [[ -z "$(pid_on_port)" ]]; then
  c_red "포트 ${PORT}에서 프로세스를 찾지 못했습니다. 실행 실패 가능성이 있습니다."
  exit 1
fi
c_green "포트 ${PORT} 리스닝 OK"

c_green "배포 완료 ✅"
echo ""
c_yellow "관리 명령어:"
echo "  - 로그 follow: tail -f ${APP_LOG}"
echo "  - PID 확인:    cat ${PID_FILE}"
echo "  - 중지:        kill \$(cat ${PID_FILE}) && rm -f ${PID_FILE}"

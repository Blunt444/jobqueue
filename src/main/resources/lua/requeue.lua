local readyKey = KEYS[1]
local activeKey = KEYS[2]
local batchSize = tonumber(ARGV[1])

local time = redis.call("TIME")
local ms = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

local expiredJobs = redis.call(
    "ZRANGEBYSCORE",
    activeKey,
    "-inf",
    ms,
    "LIMIT", 0, batchSize)

local requeued = 0

for _, job in ipairs(expiredJobs) do
    if redis.call("ZREM", activeKey, job) == 1 then
        redis.call("LPUSH", readyKey, job)
        requeued = requeued + 1
    end
end

return requeued

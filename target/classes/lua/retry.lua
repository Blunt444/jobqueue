local readyKey = KEYS[1]
local retryKey = KEYS[2]
local batchSize = tonumber(ARGV[1])

local time = redis.call("TIME")
local ms = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

local retryJobs = redis.call(
    "ZRANGEBYSCORE",
    retryKey,
    "-inf",
    ms,
    "LIMIT", 0, batchSize
)

local jobsMoved = 0

for _, job in ipairs(retryJobs) do
    if redis.call("ZREM", retryKey, job) == 1 then
        redis.call("LPUSH", readyKey, job)
        jobsMoved = jobsMoved + 1
    end
end

return jobsMoved

local retryKey = KEYS[1]
local activeKey = KEYS[2]
local attemptsKey = KEYS[3]
local deadKey = KEYS[4]
local batchSize = tonumber(ARGV[1])
local cap = tonumber(ARGV[2])
local base = tonumber(ARGV[3])

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
        local fields = {}
        for field in string.gmatch(job, "[^\t]+") do
            table.insert(fields, field)
        end

        local maxAttempts = tonumber(fields[3])
        local attempts = tonumber(redis.call("ZSCORE", attemptsKey, job))
        if attempts then
            if attempts >= maxAttempts then
                redis.call("LPUSH", deadKey, job)
                redis.call("ZREM", attemptsKey, job)
            else
                local delay = math.min(cap, base * math.pow(2, attempts - 1))
                local retryAt = ms + delay
                redis.call("ZADD", retryKey, retryAt, job)
            end
        else
            redis.call("LPUSH", deadKey, job)
        end
        requeued = requeued + 1
    end
end

return requeued

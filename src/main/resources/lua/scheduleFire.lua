local lastFiredKey = KEYS[1]
local readyKey = KEYS[2]
local leaderKey = kEYS[3]
local id = ARGV[1]
local candidate = ARGV[2]
local job = ARGV[3]
local ownerId = ARGV[4]

local key = lastFiredKey .. ":" .. id

local currentOwner = redis.call("GET", leaderKey);

if currentOwner ~= ownerId then
    return 0
end

local lastFiredTime = redis.call("GET", key)

if lastFiredTime and candidate <= lastFiredTime then
    return 0
end

redis.call("SET", key, candidate)
redis.call("LPUSH", readyKey, job);

return 1

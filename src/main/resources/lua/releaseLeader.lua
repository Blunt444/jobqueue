local leaderKey = KEYS[1]
local ownerId = ARGV[1]

local isLocked = redis.call("EXISTS", leaderKey)

if isLocked == 0 then
    return 0
end

local currentOwner = redis.call("GET", leaderKey)

if ownerId ~= currentOwner then
    return 0
end

local status = redis.call("DEL", leaderKey)

return status

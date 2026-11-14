-- Add indexes for better query performance on ticket table
CREATE INDEX IF NOT EXISTS idx_ticket_no ON ticket(ticketNo);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON ticket(status);

-- Optimize table for better performance with BLOB data
ALTER TABLE ticket ENGINE=InnoDB ROW_FORMAT=DYNAMIC;
